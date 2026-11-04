package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.AttributeDefinitionMapper;
import com.opa.permission.model.AttributeDefinition;
import com.opa.permission.service.AttributeDefinitionService;
import org.springframework.stereotype.Service;

@Service
public class AttributeDefinitionServiceImpl
        extends ServiceImpl<AttributeDefinitionMapper, AttributeDefinition>
        implements AttributeDefinitionService {
}
