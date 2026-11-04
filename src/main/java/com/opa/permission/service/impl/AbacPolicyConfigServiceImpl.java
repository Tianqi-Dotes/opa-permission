package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.AbacPolicyConfigMapper;
import com.opa.permission.model.AbacPolicyConfig;
import com.opa.permission.service.AbacPolicyConfigService;
import org.springframework.stereotype.Service;

@Service
public class AbacPolicyConfigServiceImpl
        extends ServiceImpl<AbacPolicyConfigMapper, AbacPolicyConfig>
        implements AbacPolicyConfigService {
}
