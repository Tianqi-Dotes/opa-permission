package com.opa.permission.config;

import com.opa.permission.model.Permission;
import com.opa.permission.service.PermissionDefaultAttributeInjectionService;
import com.opa.permission.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.Comparator;

@Aspect
@Component
@Order(100)
public class PermissionDefaultAttributeInjectionAspect {
    private final PermissionService permissionService;
    private final PermissionDefaultAttributeInjectionService injectionService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public PermissionDefaultAttributeInjectionAspect(
            PermissionService permissionService,
            PermissionDefaultAttributeInjectionService injectionService) {
        this.permissionService = permissionService;
        this.injectionService = injectionService;
    }

    @Around("execution(public * com.opa.permission.controller..*(..))")
    public Object injectDefaultAttributes(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        if (!isSuccessful(result)) {
            return result;
        }

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return result;
        }

        HttpServletRequest request = attributes.getRequest();
        String requestPath = StringUtils.hasText(request.getServletPath())
                ? request.getServletPath()
                : request.getRequestURI();
        Permission permission = findPermission(requestPath, request.getMethod());
        if (permission == null) {
            return result;
        }

        Object responseBody = result instanceof ResponseEntity<?> response
                ? response.getBody()
                : result;
        injectionService.injectForPermission(
                permission.getPermissionId(), currentJwt(), responseBody);
        return result;
    }

    private boolean isSuccessful(Object result) {
        return !(result instanceof ResponseEntity<?> response)
                || response.getStatusCode().is2xxSuccessful();
    }

    private Permission findPermission(String requestPath, String requestMethod) {
        Comparator<String> patternComparator = pathMatcher.getPatternComparator(requestPath);
        return permissionService.listAll().stream()
                .filter(permission -> "active".equalsIgnoreCase(permission.getStatus()))
                .filter(permission -> permission.getHttpMethod() != null
                        && permission.getHttpMethod().equalsIgnoreCase(requestMethod))
                .filter(permission -> StringUtils.hasText(permission.getResourcePattern())
                        && pathMatcher.match(permission.getResourcePattern(), requestPath))
                .sorted(Comparator.comparing(Permission::getResourcePattern, patternComparator))
                .findFirst()
                .orElse(null);
    }

    private Jwt currentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return jwtAuthentication.getToken();
        }
        return authentication != null && authentication.getPrincipal() instanceof Jwt jwt ? jwt : null;
    }
}
