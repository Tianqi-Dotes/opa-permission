package example

import rego.v1

default allow := false

allow if {
    input.user == "admin1"
}

allow if {
    input.action == "read"
}