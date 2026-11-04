package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.AbacResourceMapper;
import com.opa.permission.model.AbacResource;
import com.opa.permission.service.AbacResourceService;
import org.springframework.stereotype.Service;

@Service
public class AbacResourceServiceImpl
        extends ServiceImpl<AbacResourceMapper, AbacResource>
        implements AbacResourceService {
}
