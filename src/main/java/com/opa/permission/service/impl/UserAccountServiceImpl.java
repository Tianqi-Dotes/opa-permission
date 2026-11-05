package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.UserAccountMapper;
import com.opa.permission.model.UserAccount;
import com.opa.permission.service.UserAccountService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount>
        implements UserAccountService {
    @Override
    public UserAccount getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    public List<UserAccount> listByTenantId(Long tenantId) {
        return baseMapper.selectList(new LambdaQueryWrapper<UserAccount>()
                .select(UserAccount::getUserId, UserAccount::getUsername,
                        UserAccount::getTenantId, UserAccount::getGroupName, UserAccount::getStatus)
                .eq(UserAccount::getTenantId, tenantId)
                .orderByAsc(UserAccount::getUserId));
    }
}
