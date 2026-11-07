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

This config enables authentication only. It does not configure OPA access control or install the Trino OPA plugin. HTTPS settings are left commented in `etc/config.properties` for later.
