package com.opa.permission.service;

public interface OpaDecisionService {
    boolean bucketDecision(String action, Long resourceTenantId, String jwt);

    boolean resourceDecision(String action,
                             String resourceType,
                             String resourceId,
                             Long resourceTenantId,
                             String jwt);
}
