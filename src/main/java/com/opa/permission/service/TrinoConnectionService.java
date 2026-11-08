package com.opa.permission.service;

import com.opa.permission.model.AbacResource;
import com.opa.permission.model.TrinoConnection;

public interface TrinoConnectionService {
    CreateResult create(String connectionName, String jdbcUrl, long tenantId);

    record CreateResult(AbacResource resource, TrinoConnection connection) {
    }
}
