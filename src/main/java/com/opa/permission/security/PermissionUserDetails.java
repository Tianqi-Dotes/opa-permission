package com.opa.permission.security;

import com.opa.permission.model.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class PermissionUserDetails implements UserDetails {
    private final Long userId;
    private final Long tenantId;
    private final String groupName;
    private final String username;
    private final String passwordHash;
    private final boolean enabled;
    private final List<String> roles;

    public PermissionUserDetails(UserAccount userAccount) {
        this(userAccount, List.of());
    }

    public PermissionUserDetails(UserAccount userAccount, List<String> roles) {
        this.userId = userAccount.getUserId();
        this.tenantId = userAccount.getTenantId();
        this.groupName = userAccount.getGroupName();
        this.username = userAccount.getUsername();
        this.passwordHash = userAccount.getPasswordHash();
        this.enabled = "active".equalsIgnoreCase(userAccount.getStatus());
        this.roles = List.copyOf(roles);
    }

    public Long getUserId() {
        return userId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<String> getRoles() {
        return roles;
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
