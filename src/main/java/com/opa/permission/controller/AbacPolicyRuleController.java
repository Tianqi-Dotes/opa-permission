package com.opa.permission.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.opa.permission.model.AbacPolicyRule;
import com.opa.permission.service.AbacPolicyRuleService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/abac/policy-rules")
public class AbacPolicyRuleController {
    private final AbacPolicyRuleService abacPolicyRuleService;

    public AbacPolicyRuleController(AbacPolicyRuleService abacPolicyRuleService) {
        this.abacPolicyRuleService = abacPolicyRuleService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody(required = false) CreatePolicyRuleRequest request) {
        String validationError = validate(request);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(new ApiError(validationError));
        }

        long now = Instant.now().toEpochMilli();
        AbacPolicyRule rule = new AbacPolicyRule();
        rule.setRuleCode("rule_" + UUID.randomUUID().toString().replace("-", ""));
        updateRuleFields(rule, request);
        rule.setCreateTime(now);
        rule.setUpdateTime(now);

        try {
            if (!abacPolicyRuleService.save(rule)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiError("Policy rule was not created"));
            }
        } catch (DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiError("A policy rule with the generated rule code already exists"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(rule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id,
                                    @RequestBody(required = false) CreatePolicyRuleRequest request) {
        String validationError = validate(request);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(new ApiError(validationError));
        }

        AbacPolicyRule rule = abacPolicyRuleService.getById(id);
        if (rule == null) {
            return notFound();
        }

        updateRuleFields(rule, request);
        rule.setUpdateTime(Instant.now().toEpochMilli());
        if (!abacPolicyRuleService.updateById(rule)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("Policy rule was not updated"));
        }
        return ResponseEntity.ok(rule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        if (abacPolicyRuleService.getById(id) == null) {
            return notFound();
        }
        if (!abacPolicyRuleService.removeById(id)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("Policy rule was not deleted"));
        }
        return ResponseEntity.noContent().build();
    }

    private static void updateRuleFields(AbacPolicyRule rule, CreatePolicyRuleRequest request) {
        if (request.policyId() != null) {
            rule.setPolicyId(request.policyId());
        }
        rule.setRuleName(request.ruleName().trim());
        rule.setEffect(PolicyRuleSupport.normalize(request.effect()));
        rule.setEnabled(request.enabled() == null || request.enabled());
        rule.setConditionStr(PolicyRuleSupport.compileConditions(request.conditions()));
        rule.setExpression(StringUtils.hasText(request.expression()) ? request.expression().trim() : null);
        rule.setDescription(request.description());
    }

    private static String validate(CreatePolicyRuleRequest request) {
        if (request == null) {
            return "Request body is required";
        }
        if (request.policyId() != null && request.policyId() <= 0) {
            return "policyId must be positive";
        }
        return PolicyRuleSupport.validate(request.ruleName(), request.effect(),
                request.conditions(), request.expression());
    }

    private static ResponseEntity<ApiError> notFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("Policy rule not found"));
    }

    public record CreatePolicyRuleRequest(Long policyId,
                                          String ruleName,
                                          String effect,
                                          Boolean enabled,
                                          List<ConditionRequest> conditions,
                                          String expression,
                                          String description) {
    }

    public record ConditionRequest(String attribute, String operator, JsonNode value, String valueAttribute) {
        public ConditionRequest(String attribute, String operator, JsonNode value) {
            this(attribute, operator, value, null);
        }
    }

    public record ApiError(String message) {
    }
}
