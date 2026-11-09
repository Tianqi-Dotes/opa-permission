package com.opa.permission.controller;

import com.opa.permission.model.AttributeDefinition;
import com.opa.permission.service.AttributeDefinitionService;
import com.opa.permission.service.TrinoExternalResourceAttributeService;
import com.opa.permission.service.TrinoMetadataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trino/connections")
public class TrinoMetadataController {
    private final TrinoMetadataService trinoMetadataService;
    private final TrinoExternalResourceAttributeService trinoExternalResourceAttributeService;
    private final AttributeDefinitionService attributeDefinitionService;
    private final AttributeValueValidator valueValidator;

    public TrinoMetadataController(TrinoMetadataService trinoMetadataService,
                                   TrinoExternalResourceAttributeService trinoExternalResourceAttributeService,
                                   AttributeDefinitionService attributeDefinitionService,
                                   AttributeValueValidator valueValidator) {
        this.trinoMetadataService = trinoMetadataService;
        this.trinoExternalResourceAttributeService = trinoExternalResourceAttributeService;
        this.attributeDefinitionService = attributeDefinitionService;
        this.valueValidator = valueValidator;
    }

    @GetMapping("/{resourceId}/tree")
    public ResponseEntity<?> getTree(@PathVariable("resourceId") Long resourceId,
                                     @RequestParam(value = "catalog", required = false) String catalog,
                                     @RequestParam(value = "schema", required = false) String schema,
                                     @RequestParam(value = "table", required = false) String table,
                                     @AuthenticationPrincipal Jwt jwt) {
        if (resourceId == null || resourceId <= 0) {
            return ResponseEntity.badRequest().body(new ApiError("resourceId must be a positive integer"));
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
            return ResponseEntity.ok(
                    trinoMetadataService.listChildren(resourceId, tenantId, jwt, catalog, schema, table));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ApiError(exception.getMessage()));
        }
    }

    @PutMapping("/{resourceId}/resource-attributes")
    public ResponseEntity<?> replaceResourceAttributes(
            @PathVariable("resourceId") Long resourceId,
            @RequestBody(required = false) ReplaceResourceAttributesRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        if (resourceId == null || resourceId <= 0) {
            return ResponseEntity.badRequest().body(new ApiError("resourceId must be a positive integer"));
        }
        Long tenantId = authenticatedTenantId(jwt);
        if (tenantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiError("A valid bearer token with tenant_id is required"));
        }
        if (request == null
                || request.attributeId() == null
                || request.attributeId() <= 0
                || request.valueText() == null
                || request.selections() == null
                || request.selections().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiError("attributeId, valueText, and at least one selection are required"));
        }

        AttributeDefinition definition = attributeDefinitionService.getById(request.attributeId());
        if (definition == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiError("Attribute definition not found"));
        }
        if (!"resource".equalsIgnoreCase(definition.getCategory())) {
            return ResponseEntity.badRequest()
                    .body(new ApiError("attributeId must reference a resource attribute"));
        }
        AttributeValueValidator.ValidationResult valueResult =
                valueValidator.validate(definition, request.valueText());
        if (!valueResult.isValid()) {
            return ResponseEntity.badRequest().body(new ApiError(valueResult.error()));
        }

        try {
            TrinoExternalResourceAttributeService.ReplaceResult result =
                    trinoExternalResourceAttributeService.replace(
                            resourceId,
                            tenantId,
                            jwt,
                            request.selections(),
                            request.attributeId(),
                            valueResult.valueText());
            return ResponseEntity.ok(new ReplaceResourceAttributesResponse(result.resourceCount()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ApiError(exception.getMessage()));
        }
    }

    private static Long authenticatedTenantId(Jwt jwt) {
        return jwt == null ? null : longClaim(jwt, "tenant_id");
    }

    private static Long longClaim(Jwt jwt, String claimName) {
        Object claim = jwt.getClaims().get(claimName);
        return claim instanceof Number number ? number.longValue() : null;
    }

    public record ReplaceResourceAttributesRequest(
            Long attributeId,
            String valueText,
            List<TrinoMetadataService.NodeSelection> selections) {
    }

    public record ReplaceResourceAttributesResponse(int resourceCount) {
    }

    public record ApiError(String message) {
    }
}
