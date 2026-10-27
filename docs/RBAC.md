## Spring Security RBAC

Authentication uses `POST /auth/login` with a JSON body containing `username` and `password`. Successful login returns `accessToken`, `tokenType`, and `expiresIn`; send the token as `Authorization: Bearer <accessToken>`. User credentials are stored in `test.users`; `password_hash` must contain a BCrypt hash. Set `JWT_SECRET_BASE64` to a Base64-encoded random secret of at least 32 bytes (for example, generate one with PowerShell: `[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))`). All non-login requests require an active permission matching both the request path and HTTP method. Requests without a matching permission are denied.

Permissions may be assigned directly through `test.relation_user_permissions` or through a role using `test.relation_user_roles` and `test.relation_role_permissions`. Inactive permissions and roles are ignored. A grant with a null `tenant_id` is global; otherwise it applies only when it matches the authenticated user's fixed tenant.

`resource_pattern` uses Spring's Ant-style path patterns (for example `/api/orders/**`). A null or blank `http_method` matches any method; a null or blank resource pattern never grants access. No role/permission administration endpoints or initial user are created by this change; provision BCrypt password hashes and permission rows through your deployment or administration process. Serve the API over HTTPS in production.

The Trino query endpoint is `POST /api/trino/query` with a JSON body such as `{"sql":"SELECT 1"}` and the caller's `Authorization: Bearer <accessToken>` header. The application forwards that same JWT to Trino using the JDBC `accessToken` property; the Trino coordinator must trust the application JWT signing key, use the matching issuer (`opa-permission`), and have TLS enabled. Configure `TRINO_JDBC_URL` with TLS (the default is `jdbc:trino://localhost:8443?SSL=true`). Trino derives its principal from the JWT subject (`sub`, the login username). The query endpoint accepts one `SELECT` or `WITH` query, returns at most `trino.query.max-rows` rows, and uses `trino.query.timeout-seconds` as the JDBC query timeout. Access is authorized by both application RBAC and Trino's own access-control rules. Grant the endpoint to users through a permission with resource pattern `/api/trino/query` and HTTP method `POST`.

Assign an active role to a user with `POST /api/permissions/users/{userId}/roles` and JSON body `{"roleId":123}`. The authenticated user's tenant must match the target user's tenant; the association records the target tenant and authenticated user as `grant_by`. Duplicate assignments return HTTP 409. Protect this endpoint with an RBAC permission matching `/api/permissions/users/*/roles` and method `POST`.

# PREREQUISITES:
ADMIN is ADMIN ROLE.
Has 3 members in the system: ADMIN, RICK, SAM.
Has 3 roles in the system: ADMIN, ANALYST, DEVELOPER.
Has 4 APIs: 1 query trino, 2 create trino catalog, 3 create trino schema, 4 create trino table
ROLE ADMIN has all permissions.
ANALYST has permission to query trino.
DEVELOPER has permission to create trino catalog, schema, and table.

# TEST CASE1: Assign DEVELOPER ROLE to RICK
1 Rick cannot access create trino catalog, schema, and table APIs.
2 Assign DEVELOPER ROLE to RICK.
3 Rick can access create trino catalog, schema, and table APIs.
4 Rick create catalog.schema.tableA

## Trino DDL APIs

The DDL endpoints use `POST` and return HTTP 201 on success. Create a catalog at `/api/trino/catalogs` with `{"catalogName":"pg","connectorName":"postgresql","properties":{"connection-url":"jdbc:postgresql://db:5432/app","connection-user":"reader","connection-password":"${ENV:PG_PASSWORD}"}}`. Trino logs complete `CREATE CATALOG` statements, so secret-valued properties and DDL values must use Trino environment references such as `${ENV:PG_PASSWORD}`; configure the referenced environment variable on the Trino coordinator.

Create a schema at `/api/trino/schemas` with `{"catalogName":"iceberg","schemaName":"analytics","location":"s3://warehouse/analytics","ifNotExists":true}`; `location` is optional. Create a table at `/api/trino/tables` with `{"catalogName":"iceberg","schemaName":"analytics","tableName":"events","columns":[{"name":"id","type":"BIGINT"},{"name":"name","type":"VARCHAR(100)"}],"properties":{"format":"PARQUET"},"ifNotExists":true}`. Table columns support primitive Trino types, and table properties are optional string values. Trino's own access-control rules must also allow the operations.
