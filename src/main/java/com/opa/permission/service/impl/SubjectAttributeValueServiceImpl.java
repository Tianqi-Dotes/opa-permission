package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.SubjectAttributeValueMapper;
import com.opa.permission.model.SubjectAttributeValue;
import com.opa.permission.service.SubjectAttributeValueService;
import org.springframework.stereotype.Service;

@Service
public class SubjectAttributeValueServiceImpl
        extends ServiceImpl<SubjectAttributeValueMapper, SubjectAttributeValue>
        implements SubjectAttributeValueService {
}
