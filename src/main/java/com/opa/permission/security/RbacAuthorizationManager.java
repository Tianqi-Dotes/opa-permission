package com.opa.permission.security;

import com.opa.permission.model.Permission;
import com.opa.permission.service.PermissionService;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

@Component
public class RbacAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private final PermissionService permissionService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public RbacAuthorizationManager(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication,
                                       RequestAuthorizationContext context) {
        Authentication currentAuthentication = authentication.get();
        if (currentAuthentication == null || !currentAuthentication.isAuthenticated()
                || !(currentAuthentication.getPrincipal() instanceof Jwt jwt)) {
            return new AuthorizationDecision(false);
        }

        Long userId = getLongClaim(jwt, "user_id");
        Long tenantId = getLongClaim(jwt, "tenant_id");
        if (userId == null || tenantId == null) {
            return new AuthorizationDecision(false);
        }

        String requestPath = context.getRequest().getServletPath();
        String requestMethod = context.getRequest().getMethod();
        boolean granted = permissionService.getEffectivePermissions(userId, tenantId).stream()
                .anyMatch(permission -> matches(permission, requestPath, requestMethod));
        return new AuthorizationDecision(granted);
    }

    private Long getLongClaim(Jwt jwt, String claimName) {
        Object claim = jwt.getClaims().get(claimName);
        return claim instanceof Number number ? number.longValue() : null;
    }

    private boolean matches(Permission permission, String requestPath, String requestMethod) {
        if (!StringUtils.hasText(permission.getResourcePattern())
                || !pathMatcher.match(permission.getResourcePattern(), requestPath)) {
            return false;
        }
        return !StringUtils.hasText(permission.getHttpMethod())
                || permission.getHttpMethod().equalsIgnoreCase(requestMethod);
    }
}
