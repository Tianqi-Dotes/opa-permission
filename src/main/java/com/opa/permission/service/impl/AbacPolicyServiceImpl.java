package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.AbacPolicyMapper;
import com.opa.permission.model.AbacPolicy;
import com.opa.permission.service.AbacPolicyService;
import org.springframework.stereotype.Service;

@Service
public class AbacPolicyServiceImpl extends ServiceImpl<AbacPolicyMapper, AbacPolicy>
        implements AbacPolicyService {
}
