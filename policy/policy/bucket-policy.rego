package bucket

import rego.v1

default allow := false
default verify := false

verified_claims := claims if {
    token := input.context.identity.extraCredentials.jwt
    secret_base64 := opa.runtime().env.JWT_SECRET_BASE64
    secret := base64.decode(secret_base64)
    decoded := io.jwt.decode_verify(token, {
        "alg": "HS256",
        "iss": "opa-permission",
        "secret": secret,
    })
    decoded[0] == true
    claims := decoded[2]
}


allow if {
    input.resourceType == "bucket"
    verified_claims.tenant_id == input.resource.tenant_id
}
