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
    group_name              VARCHAR(256),
    clearance               INTEGER,
    region                  VARCHAR(128),
    status                  VARCHAR(32) NOT NULL DEFAULT 'active',
    created_time            BIGINT NOT NULL,
    updated_time            BIGINT NOT NULL
);


create table if not exists test.tenants (
    tenant_id               BIGINT PRIMARY KEY,
    tenant_name             VARCHAR(256) NOT NULL UNIQUE,
    group_name                VARCHAR(256),
    status                  VARCHAR(32) NOT NULL DEFAULT 'active',
    created_time            BIGINT NOT NULL,
    updated_time            BIGINT NOT NULL
);

insert into test.tenants (
    tenant_id,
    tenant_name,
    group_name,
    status,
    created_time,
    updated_time
) values (
    1,
    'dpaas',
    'adgroup0001',
    'active',
    (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT
);

truncate test.permissions;
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
    (4, 'Create Trino Table', '/api/trino/tables', 'POST', 'active', 'Create a Trino table', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (5, 'List Users', '/api/permissions/users', 'GET', 'active', 'List users in the authenticated user tenant', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (6, 'Assign User Role', '/api/permissions/users/*/roles', 'POST', 'active', 'Assign a role to a user in the authenticated user tenant', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (7, 'List ABAC Attributes', '/api/abac/attributes', 'GET', 'active', 'List ABAC attribute definitions', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (8, 'Create ABAC Attribute', '/api/abac/attributes', 'POST', 'active', 'Create an ABAC attribute definition', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (9, 'Read ABAC Attribute', '/api/abac/attributes/*', 'GET', 'active', 'Read an ABAC attribute definition', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (10, 'Update ABAC Attribute', '/api/abac/attributes/*', 'PUT', 'active', 'Update an ABAC attribute definition', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (11, 'Delete ABAC Attribute', '/api/abac/attributes/*', 'DELETE', 'active', 'Delete an ABAC attribute definition', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (12, 'List Subject Attribute Values', '/api/abac/subject-attributes', 'GET', 'active', 'List subject attribute values', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (13, 'Create Subject Attribute Value', '/api/abac/subject-attributes', 'POST', 'active', 'Create a subject attribute value', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (14, 'Read Subject Attribute Value', '/api/abac/subject-attributes/*', 'GET', 'active', 'Read a subject attribute value', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (15, 'Update Subject Attribute Value', '/api/abac/subject-attributes/*', 'PUT', 'active', 'Update a subject attribute value', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (16, 'Delete Subject Attribute Value', '/api/abac/subject-attributes/*', 'DELETE', 'active', 'Delete a subject attribute value', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (17, 'List Resource Attribute Values', '/api/abac/resource-attributes', 'GET', 'active', 'List resource attribute values', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (18, 'Create Resource Attribute Value', '/api/abac/resource-attributes', 'POST', 'active', 'Create a resource attribute value', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (19, 'Read Resource Attribute Value', '/api/abac/resource-attributes/*', 'GET', 'active', 'Read a resource attribute value', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (20, 'Update Resource Attribute Value', '/api/abac/resource-attributes/*', 'PUT', 'active', 'Update a resource attribute value', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (21, 'Delete Resource Attribute Value', '/api/abac/resource-attributes/*', 'DELETE', 'active', 'Delete a resource attribute value', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (22, 'Create ABAC Bucket Resource', '/api/abac/resources/buckets', 'POST', 'active', 'Register a bucket resource for the authenticated tenant', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (23, 'List ABAC Resources', '/api/abac/resources', 'GET', 'active', 'List resources owned by the authenticated tenant', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (24, 'Execute Trino SQL', '/api/trino/execute', 'POST', 'active', 'Execute one SQL statement through Trino', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (25, 'Create Trino Datasource', '/api/trino/connections', 'POST', 'active', 'Register a token-authenticated Trino JDBC datasource', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (26, 'Create ABAC Policy Rule', '/api/abac/policy-rules', 'POST', 'active', 'Create an ABAC policy rule', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (27, 'Update ABAC Policy Rule', '/api/abac/policy-rules/*', 'PUT', 'active', 'Update an ABAC policy rule', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (28, 'Delete ABAC Policy Rule', '/api/abac/policy-rules/*', 'DELETE', 'active', 'Delete an ABAC policy rule', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (29, 'Check ABAC Resource Decision', '/api/abac/resources/*/decision', 'POST', 'active', 'Check resource access using OPA', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (30, 'Generate ABAC Policy Config', '/api/abac/policies/*/config', 'GET', 'active', 'Generate a config fragment for an ABAC policy', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (31, 'Generate All ABAC Policy Configs', '/api/abac/policies/generate-config', 'POST', 'active', 'Generate and write bucket and Trino policy config files', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT)
ON CONFLICT (permission_id) DO UPDATE SET
    permission_name = EXCLUDED.permission_name,
    resource_pattern = EXCLUDED.resource_pattern,
    http_method = EXCLUDED.http_method,
    status = EXCLUDED.status,
    description = EXCLUDED.description,
    updated_time = EXCLUDED.updated_time;