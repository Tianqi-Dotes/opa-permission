# Trino with the application's JWT authentication

This is a single-node Trino coordinator configured to validate bearer tokens issued by this application's `JwtTokenService`. The service signs JWTs with HS256, sets `iss` to `opa-permission`, and uses the username as `sub`. Trino maps `sub` to the Trino user. The service currently does not issue an `aud` claim, so no audience restriction is configured.

> **Warning:** HTTP does not encrypt traffic. The bearer token can be observed by anyone who can access the network path. `http-server.authentication.allow-insecure-over-http=true` is only for local development/testing; do not expose this service to an untrusted network. Enable HTTPS before production use.

## Prepare configuration

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

Trino is available at `http://localhost:8080` by default. Send a token returned by the application's login endpoint as a bearer token. For example, with the Trino CLI:

```powershell
trino --server http://localhost:8080 --access-token "<jwt>"
```

The token's signature is checked against the shared HS256 key, its `iss` must be `opa-permission`, and its `sub` becomes the Trino user. Tokens expire after 900 seconds by default (`security.jwt.expiration-seconds`).

## OPA access control and JWT claims

The Trino image includes the OPA access-control plugin. `etc/access-control.properties` points to OPA's `trino.allow`, `trino.rowFilters`, and `trino.columnMask` decision paths. Trino sends its normal identity and action/resource context, and forwards the raw access token as the `jwt` extra credential on Backend JDBC connections. OPA receives it at `input.context.identity.extraCredentials.jwt`; it must verify the JWT signature and required claims before trusting the decoded payload. Decoding without signature verification is not sufficient.

The OPA plugin only forwards the extra-credential keys listed in `opa.identity.extra-credentials-keys`. Direct Trino clients must also provide the `jwt` extra credential; Backend JDBC connections populate it automatically. The normal Trino `accessToken` is still used for Trino authentication, while the extra credential lets OPA independently verify and parse the same JWT. Keep OPA request logging disabled to avoid logging bearer tokens.

The OPA server must expose the configured paths and return the response formats expected by Trino: a boolean for `allow`, an array of filter expressions for `rowFilters`, and a mask expression for `columnMask`. The currently running local OPA has no `data.trino` policy paths yet, so queries will be denied or fail until those rules are added. `host.docker.internal` is used for the local Docker Desktop setup; replace it with the OPA service DNS name in a shared Docker network deployment.

## Execute SQL through the backend

The backend exposes `POST /api/trino/execute` for one SQL statement of any type, including queries, DDL, and DML. The existing `POST /api/trino/query` remains read-only. Both endpoints require the application's bearer JWT, which the backend forwards to Trino for authentication and to OPA as the `jwt` extra credential.

Send a JSON body such as `{"sql":"CREATE TABLE ... "}`. The execute endpoint returns `hasResultSet`, `columns`, `rows`, `truncated`, and `updateCount`. Result rows are capped by `trino.query.max-rows` (1000 by default), and every statement is subject to `trino.query.timeout-seconds` (60 seconds by default). Only one SQL statement is executed per request; Trino/OPA permissions still determine which statements the caller may run.
