package com.opa.permission.controller;

import com.opa.permission.service.TrinoQueryService;
import com.opa.permission.service.TrinoQueryValidationException;
import com.opa.permission.service.TrinoQueryService.QueryResult;
import com.opa.permission.service.TrinoQueryService.ColumnDefinition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
        return trinoQueryService.executeReadOnlyQuery(request.sql(), requireToken(jwt));
    }

    @PostMapping("/catalogs")
    public ResponseEntity<ActionResponse> createCatalog(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody(required = false) CreateCatalogRequest request) {
        if (request == null) {
            throw new TrinoQueryValidationException("request body is required");
        }
        trinoQueryService.createCatalog(request.catalogName(), request.connectorName(),
                request.properties(), requireToken(jwt));
        return created("Catalog created");
    }

    @PostMapping("/schemas")
    public ResponseEntity<ActionResponse> createSchema(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody(required = false) CreateSchemaRequest request) {
        if (request == null) {
            throw new TrinoQueryValidationException("request body is required");
        }
        trinoQueryService.createSchema(request.catalogName(), request.schemaName(),
                request.location(), request.ifNotExists(), requireToken(jwt));
        return created("Schema created");
    }

    @PostMapping("/tables")
    public ResponseEntity<ActionResponse> createTable(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody(required = false) CreateTableRequest request) {
        if (request == null) {
            throw new TrinoQueryValidationException("request body is required");
        }
        trinoQueryService.createTable(request.catalogName(), request.schemaName(), request.tableName(),
                request.columns(), request.properties(), request.ifNotExists(), requireToken(jwt));
        return created("Table created");
    }

    private String requireToken(Jwt jwt) {
        if (jwt == null) {
            throw new TrinoQueryValidationException("A valid bearer token is required");
        }
        return jwt.getTokenValue();
    }

    private ResponseEntity<ActionResponse> created(String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ActionResponse(message));
    }

    public record QueryRequest(String sql) {
    }

    public record CreateCatalogRequest(String catalogName, String connectorName, Map<String, String> properties) {
    }

    public record CreateSchemaRequest(String catalogName, String schemaName, String location, boolean ifNotExists) {
    }

    public record CreateTableRequest(String catalogName, String schemaName, String tableName,
                                     List<ColumnDefinition> columns, Map<String, String> properties,
                                     boolean ifNotExists) {
    }

    public record ActionResponse(String message) {
    }
}
