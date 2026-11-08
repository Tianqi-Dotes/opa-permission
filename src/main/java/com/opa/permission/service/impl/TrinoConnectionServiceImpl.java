package com.opa.permission.service.impl;

import com.opa.permission.mapper.TrinoConnectionMapper;
import com.opa.permission.model.AbacResource;
import com.opa.permission.model.TrinoConnection;
import com.opa.permission.service.AbacResourceService;
import com.opa.permission.service.TrinoConnectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class TrinoConnectionServiceImpl implements TrinoConnectionService {
    private final AbacResourceService abacResourceService;
    private final TrinoConnectionMapper trinoConnectionMapper;

    public TrinoConnectionServiceImpl(AbacResourceService abacResourceService,
                                     TrinoConnectionMapper trinoConnectionMapper) {
        this.abacResourceService = abacResourceService;
        this.trinoConnectionMapper = trinoConnectionMapper;
    }

    @Override
    @Transactional
    public CreateResult create(String connectionName, String jdbcUrl, long tenantId) {
        long now = Instant.now().toEpochMilli();
        AbacResource resource = new AbacResource();
        resource.setResourceCode("tenant:" + tenantId + ":trino:"
                + connectionName.toLowerCase(Locale.ROOT));
        resource.setResourceType("trino_connection");
        resource.setResourceName(connectionName);
        resource.setOwner(Long.toString(tenantId));
        resource.setExternal(true);
        resource.setStatus("active");
        resource.setCreateTime(now);
        resource.setUpdateTime(now);
        if (!abacResourceService.save(resource)) {
            throw new IllegalStateException("Trino datasource resource was not created");
        }

        TrinoConnection connection = new TrinoConnection();
        connection.setResourceId(resource.getId());
        connection.setJdbcUrl(jdbcUrl);
        connection.setAuthType("TOKEN");
        connection.setStatus("active");
        connection.setCreateTime(now);
        connection.setUpdateTime(now);
        if (trinoConnectionMapper.insert(connection) != 1) {
            throw new IllegalStateException("Trino datasource connection was not created");
        }
        return new CreateResult(resource, connection);
    }
}
