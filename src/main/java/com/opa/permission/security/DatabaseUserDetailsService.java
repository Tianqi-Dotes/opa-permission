package com.opa.permission.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.opa.permission.model.Role;
import com.opa.permission.model.UserAccount;
import com.opa.permission.model.UserRole;
import com.opa.permission.service.RoleService;
import com.opa.permission.service.UserAccountService;
import com.opa.permission.service.UserRoleService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserAccountService userAccountService;
    private final UserRoleService userRoleService;
    private final RoleService roleService;

    public DatabaseUserDetailsService(UserAccountService userAccountService,
                                      UserRoleService userRoleService,
                                      RoleService roleService) {
        this.userAccountService = userAccountService;
        this.userRoleService = userRoleService;
        this.roleService = roleService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount userAccount = userAccountService.getByUsername(username);
        if (userAccount == null) {
            throw new UsernameNotFoundException("User not found");
        }
        List<Long> roleIds = userRoleService.list(new LambdaQueryWrapper<UserRole>()
                        .select(UserRole::getRoleId)
                        .eq(UserRole::getUserId, userAccount.getUserId())
                        .eq(UserRole::getTenantId, userAccount.getTenantId()))
                .stream()
                .map(UserRole::getRoleId)
                .distinct()
                .toList();
        List<Role> assignedRoles = roleIds.isEmpty() ? List.of() : roleService.listByIds(roleIds);
        List<String> roles = assignedRoles.stream()
                .filter(role -> "active".equalsIgnoreCase(role.getStatus()))
                .map(Role::getRoleName)
                .filter(StringUtils::hasText)
                .toList();
        return new PermissionUserDetails(userAccount, roles);
    }
}
