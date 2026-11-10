package com.opa.permission.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {
 *   "input": {
 *     "action": "read",
 *     "resource_type": "<resource-type>",
 *     "resource_id": "xxxxx",
 *     "resource": {
 *       "tenant_id": 10
 *     },
 *     "jwt": "<signed-jwt>"
 *   }
 * }
 * @param input
 */
public record OpaRequest(Input input) {
    public record Input(
            String action,
            @JsonProperty("resource_type") String resourceType,
            @JsonProperty("resource_id") String resourceId,

            Resource resource,
            String jwt) {
    }

    public record Resource(@JsonProperty("tenant_id") Long tenantId) {
    }
}
