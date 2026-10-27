CREATE SCHEMA IF NOT EXISTS test;

INSERT INTO test.roles (
    role_id,
    role_name,
    role_type,
    status,
    description,
    created_time,
    updated_time
) VALUES
    (1, 'DEVELOPER', NULL, 'active', 'Developer role', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (2, 'ADMIN', NULL, 'active', 'Administrator role', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT),
    (3, 'ANALYST', NULL, 'active', 'Data analyst role', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT, (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT);
