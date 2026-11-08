package com.opa.permission.controller;

import com.opa.permission.service.TrinoConnectionService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/trino/connections")
public class TrinoConnectionController {
    private static final Pattern CONNECTION_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,127}");
    private static final Pattern SECRET_PARAMETER =
            Pattern.compile("(?i).*(password|passwd|token|secret|credential|access.?key).*");

    private final TrinoConnectionService trinoConnectionService;

    public TrinoConnectionController(TrinoConnectionService trinoConnectionService) {
        this.trinoConnectionService = trinoConnectionService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody(required = false) CreateDatasourceRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        if (request == null || !StringUtils.hasText(request.connectionName())) {
            return badRequest("connectionName is required");
        }
        String connectionName = request.connectionName().trim();
        if (!CONNECTION_NAME.matcher(connectionName).matches()) {
            return badRequest("connectionName must be 1-128 letters, digits, dots, underscores, or hyphens");
        }
        String jdbcUrl = request.jdbcUrl() == null ? "" : request.jdbcUrl().trim();
        String jdbcUrlError = validateJdbcUrl(jdbcUrl);
        if (jdbcUrlError != null) {
            return badRequest(jdbcUrlError);
        }
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiError("A valid bearer token is required"));
        }
        Long tenantId = longClaim(jwt, "tenant_id");
        if (tenantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiError("The bearer token is missing the tenant_id claim"));
        }

        try {
            TrinoConnectionService.CreateResult result =
                    trinoConnectionService.create(connectionName, jdbcUrl, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED).body(new CreateDatasourceResponse(
                    result.connection().getId(),
                    result.resource().getId(),
                    result.resource().getResourceCode(),
                    result.resource().getResourceName(),
                    result.connection().getJdbcUrl(),
                    result.connection().getAuthType()));
        } catch (DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiError("A Trino datasource with this name already exists in the tenant"));
        }
    }

    private static String validateJdbcUrl(String jdbcUrl) {
        if (jdbcUrl.isEmpty()) {
            return "jdbcUrl is required";
        }
        if (jdbcUrl.length() > 2048) {
            return "jdbcUrl must be at most 2048 characters";
        }
        if (!jdbcUrl.toLowerCase(Locale.ROOT).startsWith("jdbc:trino://")) {
            return "jdbcUrl must use the jdbc:trino:// scheme";
        }

        try {
            URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
            if (!StringUtils.hasText(uri.getHost()) || uri.getRawUserInfo() != null || uri.getFragment() != null) {
                return "jdbcUrl must include a host and must not contain credentials or a fragment";
            }
            boolean sslEnabled = false;
            String query = uri.getRawQuery();
            if (query != null) {
                for (String parameter : query.split("&")) {
                    String[] parts = parameter.split("=", 2);
                    String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                    String value = parts.length == 2
                            ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                            : "";
                    if (SECRET_PARAMETER.matcher(key).matches()) {
                        return "jdbcUrl must not contain credentials; token authentication is supplied at runtime";
                    }
                    if ("ssl".equalsIgnoreCase(key)) {
                        if (!"true".equalsIgnoreCase(value)) {
                            return "SSL=true is required for token authentication";
                        }
                        sslEnabled = true;
                    }
                }
            }
            return sslEnabled ? null : "SSL=true is required for token authentication";
        } catch (IllegalArgumentException exception) {
            return "jdbcUrl is invalid";
        }
    }

    private static Long longClaim(Jwt jwt, String claimName) {
        Object claim = jwt.getClaims().get(claimName);
        return claim instanceof Number number ? number.longValue() : null;
    }

    private static ResponseEntity<ApiError> badRequest(String message) {
        return ResponseEntity.badRequest().body(new ApiError(message));
    }

    public record CreateDatasourceRequest(String connectionName, String jdbcUrl) {
    }

    public record CreateDatasourceResponse(Long id, Long resourceId, String resourceCode,
                                            String connectionName, String jdbcUrl, String authType) {
    }

    public record ApiError(String message) {
    }
}
