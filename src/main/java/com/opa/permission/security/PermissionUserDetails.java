package com.opa.permission.security;

import com.opa.permission.model.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class PermissionUserDetails implements UserDetails {
    private final Long userId;
    private final Long tenantId;
    private final String username;
    private final String passwordHash;
    private final boolean enabled;

    public PermissionUserDetails(UserAccount userAccount) {
        this.userId = userAccount.getUserId();
        this.tenantId = userAccount.getTenantId();
        this.username = userAccount.getUsername();
        this.passwordHash = userAccount.getPasswordHash();
        this.enabled = "active".equalsIgnoreCase(userAccount.getStatus());
    }

    public Long getUserId() {
        return userId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
