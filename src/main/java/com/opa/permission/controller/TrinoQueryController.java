package com.opa.permission.controller;

import com.opa.permission.service.TrinoQueryService;
import com.opa.permission.service.TrinoQueryValidationException;
import com.opa.permission.service.TrinoQueryService.QueryResult;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trino")
public class TrinoQueryController {
    private final TrinoQueryService trinoQueryService;

    public TrinoQueryController(TrinoQueryService trinoQueryService) {
        this.trinoQueryService = trinoQueryService;
    }

    @PostMapping("/query")
    public QueryResult query(@AuthenticationPrincipal Jwt jwt,
                             @RequestBody(required = false) QueryRequest request) {
        if (request == null || !StringUtils.hasText(request.sql())) {
            throw new TrinoQueryValidationException("sql is required");
        }
        if (jwt == null) {
            throw new TrinoQueryValidationException("A valid bearer token is required");
        }
        return trinoQueryService.executeReadOnlyQuery(request.sql(), jwt.getTokenValue());
    }

    public record QueryRequest(String sql) {
    }
}
