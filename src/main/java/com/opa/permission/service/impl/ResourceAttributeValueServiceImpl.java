package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.ResourceAttributeValueMapper;
import com.opa.permission.model.ResourceAttributeValue;
import com.opa.permission.service.ResourceAttributeValueService;
import org.springframework.stereotype.Service;

@Service
public class ResourceAttributeValueServiceImpl
        extends ServiceImpl<ResourceAttributeValueMapper, ResourceAttributeValue>
        implements ResourceAttributeValueService {
}
