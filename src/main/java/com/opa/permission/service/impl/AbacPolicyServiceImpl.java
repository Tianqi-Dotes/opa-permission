package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.AbacPolicyMapper;
import com.opa.permission.model.AbacPolicy;
import com.opa.permission.model.AbacPolicyBinding;
import com.opa.permission.model.AbacPolicyRule;
import com.opa.permission.service.AbacPolicyBindingService;
import com.opa.permission.service.AbacPolicyRuleService;
import com.opa.permission.service.AbacPolicyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AbacPolicyServiceImpl extends ServiceImpl<AbacPolicyMapper, AbacPolicy>
        implements AbacPolicyService {
    private final AbacPolicyBindingService bindingService;
    private final AbacPolicyRuleService ruleService;

    public AbacPolicyServiceImpl(AbacPolicyBindingService bindingService,
                                 AbacPolicyRuleService ruleService) {
        this.bindingService = bindingService;
        this.ruleService = ruleService;
    }

    @Override
    @Transactional
    public void createPolicyBundle(AbacPolicy policy, AbacPolicyBinding binding, List<AbacPolicyRule> rules) {
        if (!save(policy)) {
            throw new IllegalStateException("Policy was not created");
        }
        binding.setPolicyId(policy.getId());
        if (!bindingService.save(binding)) {
            throw new IllegalStateException("Policy resource binding was not created");
        }
        for (AbacPolicyRule rule : rules) {
            rule.setPolicyId(policy.getId());
            if (!ruleService.save(rule)) {
                throw new IllegalStateException("A policy rule was not created");
            }
        }
    }

    @Override
    @Transactional
    public void deletePolicyBundle(Long policyId) {
        ruleService.remove(new QueryWrapper<AbacPolicyRule>().eq("policy_id", policyId));
        bindingService.remove(new QueryWrapper<AbacPolicyBinding>().eq("policy_id", policyId));
        if (!removeById(policyId)) {
            throw new IllegalStateException("Policy was not deleted");
        }
    }
}
