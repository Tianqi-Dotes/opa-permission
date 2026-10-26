package com.opa.permission.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.opa.permission.model.UserAccount;

public interface UserAccountService extends IService<UserAccount> {
    UserAccount getByUsername(String username);
}
