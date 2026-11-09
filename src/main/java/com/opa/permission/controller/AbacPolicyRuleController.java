package com.opa.permission.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.opa.permission.model.AbacPolicyRule;
import com.opa.permission.service.AbacPolicyRuleService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/abac/policy-rules")
public class AbacPolicyRuleController {
    private static final Set<String> EFFECTS =
            Set.of("permit", "deny", "row_filtering", "column_masking");
    private static final Pattern REGO_REFERENCE =
            Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*");
    private static final int MAX_CONDITIONS = 100;

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
        rule.setPolicyId(request.policyId());
        rule.setRuleCode("rule_" + UUID.randomUUID().toString().replace("-", ""));
        rule.setRuleName(request.ruleName().trim());
        rule.setEffect(normalize(request.effect()));
        rule.setEnabled(request.enabled() == null || request.enabled());
        rule.setConditionStr(compileConditions(request.conditions()));
        rule.setExpression(StringUtils.hasText(request.expression()) ? request.expression().trim() : null);
        rule.setDescription(request.description());
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

    private static String validate(CreatePolicyRuleRequest request) {
        if (request == null) {
            return "Request body is required";
        }
        if (request.policyId() != null && request.policyId() <= 0) {
            return "policyId must be positive";
        }
        if (!StringUtils.hasText(request.ruleName()) || request.ruleName().trim().length() > 256) {
            return "ruleName is required and must be at most 256 characters";
        }
        String effect = normalize(request.effect());
        if (!EFFECTS.contains(effect)) {
            return "effect must be permit, deny, row_filtering, or column_masking";
        }
        if (request.conditions() == null || request.conditions().isEmpty()) {
            return "At least one condition is required";
        }
        if (request.conditions().size() > MAX_CONDITIONS) {
            return "A rule may contain at most 100 conditions";
        }
        for (int index = 0; index < request.conditions().size(); index++) {
            String conditionError = validateCondition(request.conditions().get(index), index);
            if (conditionError != null) {
                return conditionError;
            }
        }
        if ((effect.equals("row_filtering") || effect.equals("column_masking"))
                && !StringUtils.hasText(request.expression())) {
            return "expression is required for row_filtering and column_masking effects";
        }
        return null;
    }

    private static String validateCondition(ConditionRequest condition, int index) {
        String prefix = "conditions[" + index + "]";
        if (condition == null) {
            return prefix + " is required";
        }
        String attribute = condition.attribute() == null ? "" : condition.attribute().trim();
        if (!REGO_REFERENCE.matcher(attribute).matches()) {
            return prefix + ".attribute must be a dotted Rego reference, such as verified_claims.clearance";
        }
        String operator = regoOperator(condition.operator());
        if (operator == null) {
            return prefix + ".operator must be equals, less_than, or greater_than";
        }
        JsonNode value = condition.value();
        if (value == null || value.isContainerNode()) {
            return prefix + ".value must be a JSON scalar";
        }
        if (!operator.equals("==") && !value.isNumber() && !value.isTextual()) {
            return prefix + ".value must be a number or string for less_than and greater_than";
        }
        return null;
    }

    private static String compileConditions(List<ConditionRequest> conditions) {
        return conditions.stream()
                .map(condition -> condition.attribute().trim()
                        + " " + regoOperator(condition.operator())
                        + " " + condition.value())
                .collect(Collectors.joining("\n"));
    }

    private static String regoOperator(String operator) {
        String normalized = normalize(operator);
        return switch (normalized) {
            case "eq", "equals", "==" -> "==";
            case "lt", "less_than", "<" -> "<";
            case "gt", "greater_than", ">" -> ">";
            default -> null;
        };
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public record CreatePolicyRuleRequest(Long policyId,
                                          String ruleName,
                                          String effect,
                                          Boolean enabled,
                                          List<ConditionRequest> conditions,
                                          String expression,
                                          String description) {
    }

    public record ConditionRequest(String attribute, String operator, JsonNode value) {
    }

    public record ApiError(String message) {
    }
}
