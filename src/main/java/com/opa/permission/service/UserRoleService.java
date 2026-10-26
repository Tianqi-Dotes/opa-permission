package com.opa.permission.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.opa.permission.model.UserRole;
import java.util.List;

public interface UserRoleService extends IService<UserRole> {
    List<UserRole> listAll();
}
