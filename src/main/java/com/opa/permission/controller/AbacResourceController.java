package com.opa.permission.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.opa.permission.entity.OpaRequest;
import com.opa.permission.model.AbacResource;
import com.opa.permission.service.AbacResourceService;
import com.opa.permission.service.OpaDecisionService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/abac/resources")
public class AbacResourceController {
    private final AbacResourceService abacResourceService;
    private final OpaDecisionService opaDecisionService;

    public AbacResourceController(AbacResourceService abacResourceService,
                                  OpaDecisionService opaDecisionService) {
        this.abacResourceService = abacResourceService;
        this.opaDecisionService = opaDecisionService;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(value = "external", required = false) Boolean external,
                                  @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiError("A valid bearer token is required"));
        }

        Long tenantId = longClaim(jwt, "tenant_id");
        if (tenantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiError("The bearer token is missing the tenant_id claim"));
        }

        QueryWrapper<AbacResource> query = new QueryWrapper<AbacResource>()
                .eq("owner", tenantId.toString());
        if (external != null) {
            query.eq("external", external);
        }
        query.orderByAsc("resource_type", "resource_name", "id");
        return ResponseEntity.ok(abacResourceService.list(query));
    }

    @PostMapping("/{resourceId}/decision")
    public ResponseEntity<?> checkDecision(@PathVariable("resourceId") Long resourceId,
                                           @AuthenticationPrincipal Jwt jwt) {
        if (resourceId == null || resourceId <= 0) {
            return ResponseEntity.badRequest().body(new ApiError("resourceId must be positive"));
        }
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiError("A valid bearer token is required"));
        }

        AbacResource resource = abacResourceService.getById(resourceId);
        if (resource == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiError("Resource not found"));
        }
        if (!StringUtils.hasText(resource.getResourceType())) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("Resource type is not configured"));
        }
        Long resourceTenantId = tenantIdFromOwner(resource.getOwner());
        if (resourceTenantId == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("Resource owner is not a valid tenant ID"));
        }

        OpaDecisionService.DecisionDetails decision = opaDecisionService.resourceDecisionDetails(
                "read",
                resource.getResourceType(),
                resourceId.toString(),
                resourceTenantId,
                jwt.getTokenValue());
        return ResponseEntity.ok(new DecisionResponse(
                decision.allowed(),
                decision.requestUrl(),
                decision.requestBody(),
                decision.responseBody()));
    }

    @PostMapping("/buckets")
    public ResponseEntity<?> createBucket(@RequestBody(required = false) CreateBucketRequest request,
                                          @AuthenticationPrincipal Jwt jwt) {
        if (request == null || !StringUtils.hasText(request.bucketName())) {
            return ResponseEntity.badRequest().body(new ApiError("bucketName is required"));
        }
        String bucketName = request.bucketName().trim();
        if (bucketName.length() > 256) {
            return ResponseEntity.badRequest().body(new ApiError("bucketName must be at most 256 characters"));
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

        long now = Instant.now().toEpochMilli();
        AbacResource resource = new AbacResource();
        resource.setResourceCode("tenant:" + tenantId + ":bucket:" + bucketName);
        resource.setResourceType("bucket");
        resource.setResourceName(bucketName);
        resource.setOwner(tenantId.toString());
        resource.setExternal(false);
        resource.setStatus("active");
        resource.setCreateTime(now);
        resource.setUpdateTime(now);

        try {
            if (!abacResourceService.save(resource)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiError("Bucket resource was not created"));
            }
        } catch (DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiError("A bucket resource with this name already exists in the tenant"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    private static Long longClaim(Jwt jwt, String claimName) {
        Object claim = jwt.getClaims().get(claimName);
        return claim instanceof Number number ? number.longValue() : null;
    }

    private static Long tenantIdFromOwner(String owner) {
        if (!StringUtils.hasText(owner)) {
            return null;
        }
        try {
            long tenantId = Long.parseLong(owner);
            return tenantId > 0 ? tenantId : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public record DecisionResponse(boolean allowed,
                                   String requestUrl,
                                   OpaRequest requestBody,
                                   Map<String, Object> responseBody) {
    }

    public record CreateBucketRequest(String bucketName) {
    }

    public record ApiError(String message) {
    }
}
