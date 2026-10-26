CREATE SCHEMA IF NOT EXISTS test;

-- Change user_id or tenant_id if 1 is not the desired default in this database.
INSERT INTO test.users (
    user_id,
    username,
    password_hash,
    tenant_id,
    status,
    created_time,
    updated_time
) VALUES (
    1,
    'admin',
    '$2a$10$ssJj4pyTenmUp4veu2m6i.nf8tkV.u4ihqeg.ptfu.vHrYaHPMrAa',
    1,
    'active',
    (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT
)
