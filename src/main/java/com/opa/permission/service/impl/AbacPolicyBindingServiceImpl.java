package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.AbacPolicyBindingMapper;
import com.opa.permission.model.AbacPolicyBinding;
import com.opa.permission.service.AbacPolicyBindingService;
import org.springframework.stereotype.Service;

@Service
public class AbacPolicyBindingServiceImpl extends ServiceImpl<AbacPolicyBindingMapper, AbacPolicyBinding>
        implements AbacPolicyBindingService {
}
