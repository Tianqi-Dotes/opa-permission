package com.opa.permission.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.opa.permission.model.UserAccount;

import java.util.List;

public interface UserAccountService extends IService<UserAccount> {
    UserAccount getByUsername(String username);

    List<UserAccount> listByTenantId(Long tenantId);
}
