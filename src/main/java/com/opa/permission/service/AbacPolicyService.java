package com.opa.permission.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.opa.permission.model.AbacPolicy;
import com.opa.permission.model.AbacPolicyBinding;
import com.opa.permission.model.AbacPolicyRule;

import java.util.List;

public interface AbacPolicyService extends IService<AbacPolicy> {
    void createPolicyBundle(AbacPolicy policy, AbacPolicyBinding binding, List<AbacPolicyRule> rules);

    void deletePolicyBundle(Long policyId);
}
