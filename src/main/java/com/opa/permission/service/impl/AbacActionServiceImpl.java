package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.AbacActionMapper;
import com.opa.permission.model.AbacAction;
import com.opa.permission.service.AbacActionService;
import org.springframework.stereotype.Service;

@Service
public class AbacActionServiceImpl
        extends ServiceImpl<AbacActionMapper, AbacAction>
        implements AbacActionService {
}
