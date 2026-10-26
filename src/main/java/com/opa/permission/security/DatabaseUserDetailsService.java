package com.opa.permission.security;

import com.opa.permission.model.UserAccount;
import com.opa.permission.service.UserAccountService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserAccountService userAccountService;

    public DatabaseUserDetailsService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount userAccount = userAccountService.getByUsername(username);
        if (userAccount == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new PermissionUserDetails(userAccount);
    }
}
