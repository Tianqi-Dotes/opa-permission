package com.opa.permission.service.impl;

import com.opa.permission.service.OpaDecisionException;
import com.opa.permission.service.OpaDecisionService;
import com.opa.permission.entity.OpaRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Service
public class OpaDecisionServiceImpl implements OpaDecisionService {
    private static final String RESOURCE_TYPE_BUCKET = "bucket";

    private final RestClient restClient;
    private final String decisionUrl;

    public OpaDecisionServiceImpl(RestClient.Builder restClientBuilder,
                                  @Value("${opa.decision-url}") String decisionUrl) {
        this.restClient = restClientBuilder.build();
        this.decisionUrl = decisionUrl;
    }

    @Override
    public boolean bucketDecision(String action, Long resourceTenantId, String jwt) {
        return decide(action, RESOURCE_TYPE_BUCKET, null, resourceTenantId, jwt);
    }

    @Override
    public boolean resourceDecision(String action,
                                    String resourceType,
                                    String resourceId,
                                    Long resourceTenantId,
                                    String jwt) {
        if (!StringUtils.hasText(resourceType)) {
            throw new IllegalArgumentException("resourceType is required");
        }
        if (!StringUtils.hasText(resourceId)) {
            throw new IllegalArgumentException("resourceId is required");
        }
        return decide(action, resourceType.trim(), resourceId.trim(), resourceTenantId, jwt);
    }

    private boolean decide(String action,
                           String resourceType,
                           String resourceId,
                           Long resourceTenantId,
                           String jwt) {
        if (!StringUtils.hasText(action)) {
            throw new IllegalArgumentException("action is required");
        }
        if (!StringUtils.hasText(resourceType)) {
            throw new IllegalArgumentException("resourceType is required");
        }
        if (resourceTenantId == null || resourceTenantId <= 0) {
            throw new IllegalArgumentException("resourceTenantId must be a positive integer");
        }
        if (!StringUtils.hasText(jwt)) {
            throw new IllegalArgumentException("jwt is required");
        }

        OpaRequest request = new OpaRequest(new OpaRequest.Input(
                action.trim(),
                resourceType.trim(),
                resourceId,
                new OpaRequest.Resource(resourceTenantId),
                jwt));

        Map<String, Object> response;
        try {
            response = restClient.post()
                    .uri(decisionUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() { });
        } catch (RestClientException exception) {
            throw new OpaDecisionException("OPA decision request failed", exception);
        }

        if (response == null || !(response.get("result") instanceof Boolean allowed)) {
            throw new OpaDecisionException("OPA response did not contain a boolean result");
        }
        return allowed;
    }
}
