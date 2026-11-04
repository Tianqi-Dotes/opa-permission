package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.opa.permission.mapper.AbacAuditLogMapper;
import com.opa.permission.model.AbacAuditLog;
import com.opa.permission.service.AbacAuditLogService;
import org.springframework.stereotype.Service;

@Service
public class AbacAuditLogServiceImpl
        extends ServiceImpl<AbacAuditLogMapper, AbacAuditLog>
        implements AbacAuditLogService {
}
