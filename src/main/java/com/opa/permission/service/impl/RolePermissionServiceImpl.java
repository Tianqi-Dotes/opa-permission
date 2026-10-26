package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.RolePermissionMapper;
import com.opa.permission.model.RolePermission;
import com.opa.permission.service.RolePermissionService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {
    @Override
    public List<RolePermission> listAll() {
        return baseMapper.selectAll();
    }
}
