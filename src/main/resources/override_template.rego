package trino

import rego.v1

default allow := false
default project_allow := false
default tenant_allow := false
default effective_allow := false

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

authenticated if {
    identity := input.context.identity
    verified_claims.sub == identity.user
}



# Add project-level allow conditions as project_allow rules.
# project_allow if {
#     identity := input.context.identity
#     identity.user == "admin"
#     verified_claims.tenant_id == 1
# }

# Example resource-specific project rule to copy:
# project_allow if {
#     table := input.action.resource.table
#     table.catalogName == "local_pg"
#     table.schemaName == "test"
#     table.tableName == "users"
#     identity := input.context.identity
#     identity.user == "admin"
#     verified_claims.tenant_id == 1
# }

# Add one tenant_allow rule body per resource that should be granted:
# tenant_allow if {
#     table := input.action.resource.table
#     table.catalogName == "local_pg"
#     table.schemaName == "test"
#     table.tableName == "users"
#     verified_claims.tenant_id == 7
#     identity := input.context.identity
#     identity.user == "alice"
# }


effective_allow := true if {
    tenant_allow
} else := project_allow

allow if {
    authenticated
    effective_allow
}