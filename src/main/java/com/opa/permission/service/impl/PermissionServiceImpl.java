package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.PermissionMapper;
import com.opa.permission.model.Permission;
import com.opa.permission.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
    @Override
    public List<Permission> listAll() {
        return baseMapper.selectAll();
    }

    @Override
    public List<Permission> getEffectivePermissions(Long userId, Long tenantId) {
        return baseMapper.selectEffectivePermissions(userId, tenantId);
    }
}
