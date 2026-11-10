package com.opa.permission.service;

import com.opa.permission.entity.OpaRequest;

import java.util.Map;

public interface OpaDecisionService {
    boolean bucketDecision(String action, Long resourceTenantId, String jwt);

    boolean resourceDecision(String action,
                             String resourceType,
                             String resourceId,
                             Long resourceTenantId,
                             String jwt);

    DecisionDetails resourceDecisionDetails(String action,
                                            String resourceType,
                                            String resourceId,
                                            Long resourceTenantId,
                                            String jwt);

    record DecisionDetails(boolean allowed,
                           String requestUrl,
                           OpaRequest requestBody,
                           Map<String, Object> responseBody) {
    }
}
