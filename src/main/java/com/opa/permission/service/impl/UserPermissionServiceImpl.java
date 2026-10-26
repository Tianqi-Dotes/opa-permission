package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.UserPermissionMapper;
import com.opa.permission.model.UserPermission;
import com.opa.permission.service.UserPermissionService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserPermissionServiceImpl extends ServiceImpl<UserPermissionMapper, UserPermission> implements UserPermissionService {
    @Override
    public List<UserPermission> listAll() {
        return baseMapper.selectAll();
    }
}
