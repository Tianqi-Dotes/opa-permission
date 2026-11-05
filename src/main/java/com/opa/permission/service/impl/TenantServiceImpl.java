package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.TenantMapper;
import com.opa.permission.model.Tenant;
import com.opa.permission.service.TenantService;
import org.springframework.stereotype.Service;

@Service
public class TenantServiceImpl extends ServiceImpl<TenantMapper, Tenant> implements TenantService {
}
