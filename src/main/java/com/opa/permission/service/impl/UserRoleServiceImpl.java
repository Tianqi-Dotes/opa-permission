package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.UserRoleMapper;
import com.opa.permission.model.UserRole;
import com.opa.permission.service.UserRoleService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {
    @Override
    public List<UserRole> listAll() {
        return baseMapper.selectAll();
    }
}
