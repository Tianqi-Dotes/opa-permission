# Trino with JWT and password authentication

This is a single-node Trino coordinator configured to accept both JWTs issued by this application's `JwtTokenService` and local file-based password authentication. JWTs use HS256, `iss=opa-permission`, and the username as `sub`. The password authenticator uses the local, gitignored `password.db`; the `admin` password entry is stored as a PBKDF2 hash.

> **Warning:** The local HTTPS certificate is self-signed, and `admin/123456` is only suitable for local testing. Replace both for shared or production use. HTTP port 8080 is not published by Compose; clients connect over HTTPS on port 8443.

## Prepare configuration

The local configuration uses ignored files for credentials and private keys: `.env`, `jwt-key`, `password.db`, and `certs/trino-keystore.p12`. The Compose file passes the PostgreSQL and keystore passwords from `.env`; do not commit those values. `local_pg.properties` connects to the host's PostgreSQL server at `host.docker.internal:5432/postgres` as `admin`.

Run these commands in PowerShell from this directory to create a local 256-bit JWT key file and a separate Trino internal-communication secret:

```powershell
Copy-Item .env.example .env
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$jwtBytes = New-Object byte[] 32
$rng.GetBytes($jwtBytes)
$jwtSecret = [Convert]::ToBase64String($jwtBytes)
[System.IO.File]::WriteAllText((Join-Path (Get-Location) "jwt-key"), $jwtSecret)
$internalBytes = New-Object byte[] 32
$rng.GetBytes($internalBytes)
$internalSecret = [Convert]::ToBase64String($internalBytes)
$envText = (Get-Content .env -Raw).Replace(
    "replace-with-a-random-secret-at-least-32-characters",
    $internalSecret
)
[System.IO.File]::WriteAllText((Join-Path (Get-Location) ".env"), $envText)
```

The local `.env` also needs `TRINO_HTTPS_KEYSTORE_PASSWORD` matching the generated PKCS#12 keystore and `TRINO_PG_PASSWORD` matching the PostgreSQL `admin` account. The local password file must contain the PBKDF2 hash for the Trino `admin` account; it is mounted read-only and reloaded periodically.

Use the exact same `JWT_SECRET_BASE64` value for the Java application. From the repository root, for example:

```powershell
$env:JWT_SECRET_BASE64 = (Get-Content .\docker\trino\jwt-key -Raw).Trim()
mvn spring-boot:run
```

If Java runs in a container or another environment, inject that same value there as `JWT_SECRET_BASE64`. Do not use a different key: Trino's mounted `jwt-key` file must contain the Base64-encoded value that Java decodes and uses to sign tokens. Keep both generated files local; they are gitignored.

## Start Trino

From this directory:

```powershell
docker compose up -d
docker compose logs -f trino
```

Trino is available at `https://localhost:8443`. Since the local certificate is self-signed, use `--insecure` with the CLI for local testing. Password login:

```powershell
trino --server https://localhost:8443 --user admin --password --insecure
```

JWT login remains available as well:

```powershell
trino --server https://localhost:8443 --access-token "<jwt>" --extra-credential "jwt:<jwt>" --insecure
```

The token's signature is checked against the shared HS256 key, its `iss` must be `opa-permission`, and its `sub` becomes the Trino user. Tokens expire after 900 seconds by default (`security.jwt.expiration-seconds`).

## Local PostgreSQL catalog

The `local_pg` catalog uses the PostgreSQL connector and connects to the host database `postgres` on port 5432. For example, `SHOW SCHEMAS FROM local_pg` lists schemas, and `SELECT * FROM local_pg.test.users` reads the application's users table. The PostgreSQL role `admin` must already exist and be reachable from Docker; Trino does not create that PostgreSQL account.

## OPA access control and JWT claims

The Trino image includes the OPA access-control plugin. `etc/access-control.properties` points to OPA's `trino.allow`, `trino.rowFilters`, and `trino.columnMask` decision paths. Trino sends its normal identity and action/resource context, and forwards the raw access token as the `jwt` extra credential on Backend JDBC connections. OPA receives it at `input.context.identity.extraCredentials.jwt`; it must verify the JWT signature and required claims before trusting the decoded payload. Decoding without signature verification is not sufficient.

The OPA plugin only forwards the extra-credential keys listed in `opa.identity.extra-credentials-keys`. Direct Trino clients must also provide the `jwt` extra credential; Backend JDBC connections populate it automatically. The normal Trino `accessToken` is still used for Trino authentication, while the extra credential lets OPA independently verify and parse the same JWT. Password authentication does not provide a JWT to OPA; when policies require JWT claims, use JWT login or provide a separately issued JWT as the `jwt` extra credential.

The local OPA policy verifies the JWT with `io.jwt.decode_verify`, using `JWT_SECRET_BASE64` from the OPA container environment. Set it to the same Base64 key used by Spring and Trino before starting OPA. From the repository root in PowerShell:

```powershell
$env:JWT_SECRET_BASE64 = (Get-Content .\docker\trino\jwt-key -Raw).Trim()
docker compose -f .\docker\docker-compose.yml up -d --force-recreate opa
```

The local Compose configuration enables decision logs on the console for debugging. These logs include the raw JWT extra credential; protect them and disable decision logging when it is no longer needed.

The current `policy/policy/policy.rego` checks `tenant_allow` first: a matching tenant allow returns `true`; otherwise it falls back to `project_allow`. Add independent `tenant_allow if { ... }` rules for resource-specific grants. This is allow-only precedence: tenant rules do not explicitly deny a request that the project rule allows. Trino's OPA input does not include this application's resource ID, so table rules distinguish resources by fields such as catalog, schema, and table name. The starter project rule permits the authenticated `admin` user when the verified JWT subject matches and `tenant_id` is `1`. The OPA Compose service downloads `master/bundle.tar.gz` from GitHub, so publish the updated policy bundle and wait for OPA's polling interval before expecting local policy edits to take effect. The OPA server must expose the configured paths and return the response formats expected by Trino: a boolean for `allow`, an array of filter expressions for `rowFilters`, and a mask expression for `columnMask`. `host.docker.internal` is used for the local Docker Desktop setup; replace it with the OPA service DNS name in a shared Docker network deployment.

## Execute SQL through the backend

The backend exposes `POST /api/trino/execute` for one SQL statement of any type, including queries, DDL, and DML. The existing `POST /api/trino/query` remains read-only. Both endpoints require the application's bearer JWT, which the backend forwards to Trino for authentication and to OPA as the `jwt` extra credential.

Send a JSON body such as `{"sql":"CREATE TABLE ... "}`. The execute endpoint returns `hasResultSet`, `columns`, `rows`, `truncated`, and `updateCount`. Result rows are capped by `trino.query.max-rows` (1000 by default), and every statement is subject to `trino.query.timeout-seconds` (60 seconds by default). Only one SQL statement is executed per request; Trino/OPA permissions still determine which statements the caller may run.
