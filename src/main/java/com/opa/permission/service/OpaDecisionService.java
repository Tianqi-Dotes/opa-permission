package com.opa.permission.service;

public interface OpaDecisionService {
    boolean bucketDecision(String action, Long resourceTenantId, String jwt);
}
