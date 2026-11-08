package com.opa.permission.service.impl;

import com.opa.permission.service.OpaDecisionException;
import com.opa.permission.service.OpaDecisionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OpaDecisionServiceImpl implements OpaDecisionService {
    private static final String RESOURCE_TYPE_BUCKET = "bucket";

    private final RestClient restClient;
    private final String decisionUrl;

    public OpaDecisionServiceImpl(RestClient.Builder restClientBuilder,
                                  @Value("${opa.bucket-decision-url}") String decisionUrl) {
        this.restClient = restClientBuilder.build();
        this.decisionUrl = decisionUrl;
    }

    @Override
    public boolean bucketDecision(String action, Long resourceTenantId, String jwt) {
        if (!StringUtils.hasText(action)) {
            throw new IllegalArgumentException("action is required");
        }
        if (resourceTenantId == null || resourceTenantId <= 0) {
            throw new IllegalArgumentException("resourceTenantId must be a positive integer");
        }
        if (!StringUtils.hasText(jwt)) {
            throw new IllegalArgumentException("jwt is required");
        }

        Map<String, Object> resource = Map.of("tenant_id", resourceTenantId);
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("action", action.trim());
        input.put("resource_type", RESOURCE_TYPE_BUCKET);
        input.put("resource", resource);
        input.put("jwt", jwt);
        Map<String, Object> request = Map.of("input", input);

        Map<String, Object> response;
        try {
            response = restClient.post()
                    .uri(decisionUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() { });
        } catch (RestClientException exception) {
            throw new OpaDecisionException("OPA bucket decision request failed", exception);
        }

        if (response == null || !(response.get("result") instanceof Boolean allowed)) {
            throw new OpaDecisionException("OPA response did not contain a boolean result");
        }
        return allowed;
    }
}
