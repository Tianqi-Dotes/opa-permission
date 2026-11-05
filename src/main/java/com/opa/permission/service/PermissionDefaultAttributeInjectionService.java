package com.opa.permission.service;

import org.springframework.security.oauth2.jwt.Jwt;

public interface PermissionDefaultAttributeInjectionService {
    void injectForPermission(Long permissionId, Jwt jwt, Object responseBody);
}
