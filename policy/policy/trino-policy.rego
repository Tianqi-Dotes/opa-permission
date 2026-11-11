package trino

import rego.v1

default allow := false

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
    identity := input.context.identity
    verified_claims.sub == identity.user
}

allow if {
    input.action.operation == "SetSystemSessionProperty"
    input.action.resource.systemSessionProperty.name == "query_max_run_time"
}

###### default rules

allow if {
    input.action.operation == "ExecuteQuery"
}

allow if {
    input.action.operation == "AccessCatalog"
}


#### customized rules
allow if {
    input.action.operation == "SelectFromColumns"
}



columnMask := {"expression": "CAST(concat('**', substr(customer_name, 3)) AS varchar(100))"} if {
    verified_claims.clearance == 3

    column := input.action.resource.column
    column.catalogName == "local_pg"
    column.schemaName == "public"
    column.tableName == "customers"
    column.columnName == "customer_name"
}

rowFilters contains {"expression": sprintf("country_code = '%s'", [region])} if {
    table := input.action.resource.table
    table.catalogName == "local_pg"
    table.schemaName == "public"
    table.tableName == "customers"

    region := verified_claims.region
#    escapedRegion := replace(region, "'", "''")
}

