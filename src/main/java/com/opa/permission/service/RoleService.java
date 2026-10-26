package com.opa.permission.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.opa.permission.model.Role;
import java.util.List;

public interface RoleService extends IService<Role> {
    List<Role> listAll();
}
