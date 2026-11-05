package com.opa.permission.controller;

import com.opa.permission.model.AbacResource;
import com.opa.permission.service.AbacResourceService;
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

import java.time.Instant;

@RestController
@RequestMapping("/api/abac/resources")
public class AbacResourceController {
    private final AbacResourceService abacResourceService;

    public AbacResourceController(AbacResourceService abacResourceService) {
        this.abacResourceService = abacResourceService;
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
        resource.setOwner("tenant:" + tenantId);
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

    public record CreateBucketRequest(String bucketName) {
    }

    public record ApiError(String message) {
    }
}
