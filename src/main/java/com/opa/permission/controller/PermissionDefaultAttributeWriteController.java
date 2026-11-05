package com.opa.permission.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.opa.permission.model.Permission;
import com.opa.permission.model.PermissionDefaultAttributeWrite;
import com.opa.permission.service.PermissionDefaultAttributeWriteService;
import com.opa.permission.service.PermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/abac/permission-default-attribute-writes")
public class PermissionDefaultAttributeWriteController {
    private static final Set<String> FROM_TYPES = Set.of("project", "tenant");
    private static final Set<String> TARGET_TYPES = Set.of("subject", "resource");

    private final PermissionDefaultAttributeWriteService permissionDefaultAttributeWriteService;
    private final PermissionService permissionService;

    public PermissionDefaultAttributeWriteController(
            PermissionDefaultAttributeWriteService permissionDefaultAttributeWriteService,
            PermissionService permissionService) {
        this.permissionDefaultAttributeWriteService = permissionDefaultAttributeWriteService;
        this.permissionService = permissionService;
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(value = "permissionId", required = false) Long permissionId,
            @RequestParam(value = "fromType", required = false) String fromType,
            @RequestParam(value = "targetType", required = false) String targetType,
            @RequestParam(value = "shouldInject", required = false) Boolean shouldInject) {
        String normalizedFromType = normalize(fromType);
        if (StringUtils.hasText(normalizedFromType) && !FROM_TYPES.contains(normalizedFromType)) {
            return badRequest("fromType must be project or tenant");
        }

        String normalizedTargetType = normalize(targetType);
        if (StringUtils.hasText(normalizedTargetType) && !TARGET_TYPES.contains(normalizedTargetType)) {
            return badRequest("targetType must be subject or resource");
        }

        QueryWrapper<PermissionDefaultAttributeWrite> query = new QueryWrapper<>();
        if (permissionId != null) {
            query.eq("permission_id", permissionId);
        }
        if (StringUtils.hasText(normalizedFromType)) {
            query.eq("from_type", normalizedFromType);
        }
        if (StringUtils.hasText(normalizedTargetType)) {
            query.eq("target_type", normalizedTargetType);
        }
        if (shouldInject != null) {
            query.eq("should_inject", shouldInject);
        }
        query.orderByAsc("permission_id", "from_type", "target_type", "id");
        Map<Long, Permission> permissionsById = permissionService.listAll().stream()
                .collect(Collectors.toMap(
                        Permission::getPermissionId,
                        Function.identity(),
                        (first, ignored) -> first));
        return ResponseEntity.ok(permissionDefaultAttributeWriteService.list(query).stream()
                .map(write -> toResponse(write, permissionsById.get(write.getPermissionId())))
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Long id) {
        PermissionDefaultAttributeWrite write = permissionDefaultAttributeWriteService.getById(id);
        if (write == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiError("Permission default attribute write not found"));
        }
        return ResponseEntity.ok(toResponse(write, permissionService.getById(write.getPermissionId())));
    }

    private static PermissionDefaultAttributeWriteResponse toResponse(
            PermissionDefaultAttributeWrite write, Permission permission) {
        return new PermissionDefaultAttributeWriteResponse(
                write.getId(),
                write.getPermissionId(),
                permission == null ? null : permission.getPermissionName(),
                permission == null ? null : permission.getResourcePattern(),
                permission == null ? null : permission.getHttpMethod(),
                write.getFromType(),
                write.getTargetType(),
                write.getAttributeId(),
                write.getDefaultValueText(),
                write.getDefaultValueJson(),
                write.getShouldInject(),
                write.getCreateTime(),
                write.getUpdateTime());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static ResponseEntity<ApiError> badRequest(String message) {
        return ResponseEntity.badRequest().body(new ApiError(message));
    }

    public record ApiError(String message) {
    }

    public record PermissionDefaultAttributeWriteResponse(
            Long id,
            Long permissionId,
            String permissionName,
            String permissionPath,
            String httpMethod,
            String fromType,
            String targetType,
            Long attributeId,
            String defaultValueText,
            Object defaultValueJson,
            Boolean shouldInject,
            Long createTime,
            Long updateTime) {
    }
}
