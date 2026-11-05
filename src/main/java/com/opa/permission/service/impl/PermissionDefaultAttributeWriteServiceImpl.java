package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.PermissionDefaultAttributeWriteMapper;
import com.opa.permission.model.PermissionDefaultAttributeWrite;
import com.opa.permission.service.PermissionDefaultAttributeWriteService;
import org.springframework.stereotype.Service;

@Service
public class PermissionDefaultAttributeWriteServiceImpl
        extends ServiceImpl<PermissionDefaultAttributeWriteMapper, PermissionDefaultAttributeWrite>
        implements PermissionDefaultAttributeWriteService {
}
