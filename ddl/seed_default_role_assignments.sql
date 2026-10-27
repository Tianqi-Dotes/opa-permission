CREATE SCHEMA IF NOT EXISTS test;

INSERT INTO test.relation_user_roles (
    id,
    user_id,
    tenant_id,
    role_id,
    grant_by,
    created_time,
    updated_time
)
SELECT
    COALESCE((SELECT MAX(id) FROM test.relation_user_roles), 0) + 1,
    u.user_id,
    u.tenant_id,
    r.role_id,
    NULL,
    (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT
FROM test.users u
JOIN test.roles r ON r.role_name = 'ADMIN'
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM test.relation_user_roles ur
      WHERE ur.user_id = u.user_id
        AND ur.role_id = r.role_id
  );

WITH missing_role_permissions AS (
    SELECT
        r.role_id,
        p.permission_id,
        ROW_NUMBER() OVER (ORDER BY p.permission_id) AS sequence_number
    FROM test.roles r
    CROSS JOIN test.permissions p
    WHERE (
           (r.role_name = 'ADMIN'
            AND p.permission_name IN (
                'Query Trino',
                'Create Trino Catalog',
                'Create Trino Schema',
                'Create Trino Table'
            ))
       OR (r.role_name = 'ANALYST' AND p.permission_name = 'Query Trino')
       OR (r.role_name = 'DEVELOPER'
            AND p.permission_name IN (
                'Create Trino Catalog',
                'Create Trino Schema',
                'Create Trino Table'
            ))
      )
      AND NOT EXISTS (
          SELECT 1
          FROM test.relation_role_permissions rp
          WHERE rp.role_id = r.role_id
            AND rp.permission_id = p.permission_id
      )
)
INSERT INTO test.relation_role_permissions (
    id,
    role_id,
    permission_id,
    created_time,
    updated_time
)
SELECT
    COALESCE((SELECT MAX(id) FROM test.relation_role_permissions), 0) + sequence_number,
    role_id,
    permission_id,
    (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT,
    (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT
FROM missing_role_permissions
ON CONFLICT (role_id, permission_id) DO NOTHING;
