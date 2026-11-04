package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.AbacPolicyRuleMapper;
import com.opa.permission.model.AbacPolicyRule;
import com.opa.permission.service.AbacPolicyRuleService;
import org.springframework.stereotype.Service;

@Service
public class AbacPolicyRuleServiceImpl
        extends ServiceImpl<AbacPolicyRuleMapper, AbacPolicyRule>
        implements AbacPolicyRuleService {
}
