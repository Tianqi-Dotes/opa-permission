CREATE SCHEMA IF NOT EXISTS test;

CREATE TABLE IF NOT EXISTS test.roles (
                                          role_id                 BIGINT PRIMARY KEY,
    -- role_code            VARCHAR(128) NOT NULL UNIQUE,
                                          role_name               VARCHAR(256) NOT NULL,
                                          role_type               VARCHAR(64), --default admin/tenant
                                          status                  VARCHAR(32) NOT NULL DEFAULT 'active',
    description             TEXT,
    created_time            BIGINT NOT NULL ,
    updated_time            BIGINT NOT NULL
    );

CREATE TABLE IF NOT EXISTS test.permissions (
                                                permission_id           BIGINT PRIMARY KEY,
    -- permission_code     VARCHAR(128) NOT NULL UNIQUE,
                                                permission_name         VARCHAR(256) NOT NULL,
                                                resource_pattern        VARCHAR(256), --uri pattern
                                                http_method             VARCHAR(16),
    status                  VARCHAR(32) NOT NULL DEFAULT 'active',
    description             TEXT,
    created_time            BIGINT NOT NULL ,
    updated_time            BIGINT NOT NULL
    );

CREATE TABLE IF NOT EXISTS test.relation_role_permissions (
                                                              id                      BIGINT PRIMARY KEY,
                                                              role_id                 BIGINT NOT NULL ,
                                                              permission_id           BIGINT NOT NULL ,
                                                              created_time            BIGINT NOT NULL ,
                                                              updated_time            BIGINT NOT NULL ,
                                                              CONSTRAINT uq_role_permissions UNIQUE (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS test.relation_user_permissions (
                                                              id                      BIGINT PRIMARY KEY,
                                                              user_id                 BIGINT NOT NULL ,
                                                              tenant_id               BIGINT,
                                                              permission_id           BIGINT NOT NULL ,
                                                              grant_by                BIGINT,
                                                              created_time            BIGINT NOT NULL ,
                                                              updated_time            BIGINT NOT NULL ,
                                                              CONSTRAINT uq_user_permissions UNIQUE (user_id, permission_id)
);

CREATE TABLE IF NOT EXISTS test.relation_user_roles (
                                                        id                      BIGINT PRIMARY KEY,
                                                        user_id                 BIGINT NOT NULL ,
                                                        tenant_id               BIGINT,
                                                        role_id                 BIGINT NOT NULL ,
                                                        grant_by                BIGINT,
                                                        created_time            BIGINT NOT NULL ,
                                                        updated_time            BIGINT NOT NULL ,
                                                        CONSTRAINT uq_user_roles UNIQUE (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS test.users (
    user_id                 BIGINT PRIMARY KEY,
    username                VARCHAR(128) NOT NULL UNIQUE,
    password_hash           VARCHAR(255) NOT NULL,
    tenant_id               BIGINT NOT NULL,
    status                  VARCHAR(32) NOT NULL DEFAULT 'active',
    created_time            BIGINT NOT NULL,
    updated_time            BIGINT NOT NULL
);

INSERT INTO test.permissions (
    permission_id,
    permission_name,
    resource_pattern,
    http_method,
    status,
    description,
    created_time,
    updated_time
) VALUES
    (1, 'Query Trino', '/api/trino/query', 'POST', 'active', 'Run a read-only Trino query', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (2, 'Create Trino Catalog', '/api/trino/catalogs', 'POST', 'active', 'Create a Trino catalog', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (3, 'Create Trino Schema', '/api/trino/schemas', 'POST', 'active', 'Create a Trino schema', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (4, 'Create Trino Table', '/api/trino/tables', 'POST', 'active', 'Create a Trino table', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT)
ON CONFLICT (permission_id) DO UPDATE SET
    permission_name = EXCLUDED.permission_name,
    resource_pattern = EXCLUDED.resource_pattern,
    http_method = EXCLUDED.http_method,
    status = EXCLUDED.status,
    description = EXCLUDED.description,
    updated_time = EXCLUDED.updated_time;