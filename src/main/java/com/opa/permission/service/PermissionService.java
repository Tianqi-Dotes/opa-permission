package com.opa.permission.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.opa.permission.model.Permission;

import java.util.List;

public interface PermissionService extends IService<Permission> {
    List<Permission> listAll();

    List<Permission> getEffectivePermissions(Long userId, Long tenantId);
}
