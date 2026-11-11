package com.opa.permission.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class PolicyRuleSupport {
    private static final Set<String> EFFECTS =
            Set.of("permit", "deny", "row_filtering", "column_masking");
    private static final Pattern REGO_REFERENCE =
            Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*");
    private static final int MAX_CONDITIONS = 100;

    private PolicyRuleSupport() {
    }

    static String validate(String ruleName,
                           String effect,
                           List<AbacPolicyRuleController.ConditionRequest> conditions,
                           String expression) {
        if (!StringUtils.hasText(ruleName) || ruleName.trim().length() > 256) {
            return "ruleName is required and must be at most 256 characters";
        }
        String normalizedEffect = normalize(effect);
        if (!EFFECTS.contains(normalizedEffect)) {
            return "effect must be permit, deny, row_filtering, or column_masking";
        }
        if (conditions == null || conditions.isEmpty()) {
            return "At least one condition is required";
        }
        if (conditions.size() > MAX_CONDITIONS) {
            return "A rule may contain at most 100 conditions";
        }
        for (int index = 0; index < conditions.size(); index++) {
            String conditionError = validateCondition(conditions.get(index), index);
            if (conditionError != null) {
                return conditionError;
            }
        }
        if ((normalizedEffect.equals("row_filtering") || normalizedEffect.equals("column_masking"))
                && !StringUtils.hasText(expression)) {
            return "expression is required for row_filtering and column_masking effects";
        }
        return null;
    }

    static String compileConditions(List<AbacPolicyRuleController.ConditionRequest> conditions) {
        return conditions.stream()
                .map(PolicyRuleSupport::compileCondition)
                .collect(Collectors.joining("\n"));
    }

    static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String validateCondition(AbacPolicyRuleController.ConditionRequest condition, int index) {
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
            return prefix + ".operator must be eq, neq, gt, gte, lt, lte, in, or exists";
        }
        JsonNode value = condition.value();
        String valueAttribute = condition.valueAttribute() == null ? "" : condition.valueAttribute().trim();
        if (operator.equals("exists")) {
            return value == null && valueAttribute.isEmpty()
                    ? null
                    : prefix + " with exists must not include a value";
        }
        if (!valueAttribute.isEmpty() && !REGO_REFERENCE.matcher(valueAttribute).matches()) {
            return prefix + ".valueAttribute must be a dotted Rego reference";
        }
        if ((value == null) == valueAttribute.isEmpty()) {
            return prefix + " must include exactly one of value or valueAttribute";
        }
        if (value != null && operator.equals("in") && !value.isArray()) {
            return prefix + ".value must be a JSON array for the in operator";
        }
        if (value != null && !operator.equals("in") && value.isContainerNode()) {
            return prefix + ".value must be a JSON scalar";
        }
        if (value != null && isComparisonOperator(operator)
                && !value.isNumber() && !value.isTextual()) {
            return prefix + ".value must be a number or string for comparison operators";
        }
        return null;
    }

    private static String compileCondition(AbacPolicyRuleController.ConditionRequest condition) {
        String attribute = compileReference(condition.attribute());
        String operator = regoOperator(condition.operator());
        if (operator.equals("exists")) {
            return attribute + " != null";
        }
        String value = StringUtils.hasText(condition.valueAttribute())
                ? compileReference(condition.valueAttribute())
                : condition.value().toString();
        return attribute + " " + operator + " " + value;
    }

    private static String compileReference(String reference) {
        String normalized = reference.trim();
        if (normalized.startsWith("input.subject.")) {
            return "verified_claims." + normalized.substring("input.subject.".length());
        }
        if (normalized.startsWith("subject.")) {
            return "verified_claims." + normalized.substring("subject.".length());
        }
        if (normalized.startsWith("resource.")) {
            return "input." + normalized;
        }
        if (normalized.equals("action") || normalized.startsWith("action.")
                || normalized.equals("input.action") || normalized.startsWith("input.action.")) {
            return "input.action";
        }
        return normalized;
    }

    private static boolean isComparisonOperator(String operator) {
        return operator.equals("<") || operator.equals(">") || operator.equals("<=") || operator.equals(">=");
    }

    private static String regoOperator(String operator) {
        return switch (normalize(operator)) {
            case "eq", "equals", "==" -> "==";
            case "neq", "not_equal", "!=" -> "!=";
            case "lt", "less_than", "<" -> "<";
            case "lte", "less_than_or_equal", "<=" -> "<=";
            case "gt", "greater_than", ">" -> ">";
            case "gte", "greater_than_or_equal", ">=" -> ">=";
            case "in" -> "in";
            case "exists" -> "exists";
            default -> null;
        };
    }
}
