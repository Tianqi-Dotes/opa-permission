package com.opa.permission.controller;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.opa.permission.model.Role;
import com.opa.permission.model.UserAccount;
import com.opa.permission.model.UserRole;
import com.opa.permission.service.RoleService;
import com.opa.permission.service.UserAccountService;
import com.opa.permission.service.UserRoleService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
    private final UserAccountService userAccountService;
    private final RoleService roleService;
    private final UserRoleService userRoleService;

    public PermissionController(UserAccountService userAccountService,
                                RoleService roleService,
                                UserRoleService userRoleService) {
        this.userAccountService = userAccountService;
        this.roleService = roleService;
        this.userRoleService = userRoleService;
    }

    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<?> assignRole(@PathVariable Long userId,
                                        @RequestBody(required = false) AssignRoleRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        if (request == null || request.roleId() == null) {
            return ResponseEntity.badRequest().body(new ApiError("roleId is required"));
        }
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiError("A valid bearer token is required"));
        }

        Long grantBy = longClaim(jwt, "user_id");
        Long currentTenantId = longClaim(jwt, "tenant_id");
        if (grantBy == null || currentTenantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiError("The bearer token is missing required identity claims"));
        }

        UserAccount targetUser = userAccountService.getById(userId);
        if (targetUser == null || !currentTenantId.equals(targetUser.getTenantId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiError("User not found"));
        }

        Role role = roleService.getById(request.roleId());
        if (role == null || !StringUtils.hasText(role.getStatus())
                || !"active".equalsIgnoreCase(role.getStatus())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiError("Active role not found"));
        }

        long now = Instant.now().toEpochMilli();
        UserRole userRole = new UserRole();
        userRole.setId(IdWorker.getId());
        userRole.setUserId(targetUser.getUserId());
        userRole.setTenantId(targetUser.getTenantId());
        userRole.setRoleId(role.getRoleId());
        userRole.setGrantBy(grantBy);
        userRole.setCreatedTime(now);
        userRole.setUpdatedTime(now);

        try {
            if (!userRoleService.save(userRole)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiError("Role assignment was not created"));
            }
        } catch (DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiError("Role is already assigned to this user"));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RoleAssignmentResponse(userRole.getId(), userRole.getUserId(),
                        userRole.getRoleId(), userRole.getTenantId(), userRole.getGrantBy()));
    }

    private Long longClaim(Jwt jwt, String claimName) {
        Object claim = jwt.getClaims().get(claimName);
        return claim instanceof Number number ? number.longValue() : null;
    }

    public record AssignRoleRequest(Long roleId) {
    }

    public record RoleAssignmentResponse(Long id, Long userId, Long roleId, Long tenantId, Long grantBy) {
    }

    public record ApiError(String message) {
    }
}
