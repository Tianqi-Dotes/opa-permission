package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.AbacPolicyTestCaseMapper;
import com.opa.permission.model.AbacPolicyTestCase;
import com.opa.permission.service.AbacPolicyTestCaseService;
import org.springframework.stereotype.Service;

@Service
public class AbacPolicyTestCaseServiceImpl
        extends ServiceImpl<AbacPolicyTestCaseMapper, AbacPolicyTestCase>
        implements AbacPolicyTestCaseService {
}
