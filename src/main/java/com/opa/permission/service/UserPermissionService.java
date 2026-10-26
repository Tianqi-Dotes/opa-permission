package com.opa.permission.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.opa.permission.model.UserPermission;
import java.util.List;

public interface UserPermissionService extends IService<UserPermission> {
    List<UserPermission> listAll();
}
