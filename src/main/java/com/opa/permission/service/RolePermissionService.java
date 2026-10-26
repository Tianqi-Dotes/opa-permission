package com.opa.permission.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.opa.permission.model.RolePermission;
import java.util.List;

public interface RolePermissionService extends IService<RolePermission> {
    List<RolePermission> listAll();
}
