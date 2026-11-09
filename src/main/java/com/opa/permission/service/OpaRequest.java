package com.opa.permission.service;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {
 *   "input": {
 *     "action": "read",
 *     "resource_type": "bucket",
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
            Resource resource,
            String jwt) {
    }

    public record Resource(@JsonProperty("tenant_id") Long tenantId) {
    }
}
