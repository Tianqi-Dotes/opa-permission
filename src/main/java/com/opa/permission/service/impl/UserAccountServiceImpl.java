package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.UserAccountMapper;
import com.opa.permission.model.UserAccount;
import com.opa.permission.service.UserAccountService;
import org.springframework.stereotype.Service;

@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount>
        implements UserAccountService {
    @Override
    public UserAccount getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }
}
