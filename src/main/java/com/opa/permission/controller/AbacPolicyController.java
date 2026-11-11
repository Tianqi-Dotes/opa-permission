package com.opa.permission.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.opa.permission.model.AbacPolicy;
import com.opa.permission.model.AbacPolicyBinding;
import com.opa.permission.model.AbacPolicyRule;
import com.opa.permission.model.AbacResource;
import com.opa.permission.service.AbacPolicyBindingService;
import com.opa.permission.service.AbacPolicyRuleService;
import com.opa.permission.service.AbacPolicyService;
import com.opa.permission.service.AbacResourceService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/abac/policies")
public class AbacPolicyController {
    private static final long DEFAULT_CONFIG_ID = 1L;
    private static final Set<String> COMBINING_ALGORITHMS =
            Set.of("deny_overrides", "permit_overrides", "first_applicable");
    private static final Set<String> TRINO_RESOURCE_TYPES =
            Set.of("trino_catalog", "trino_schema", "trino_table", "trino_field");

    private final AbacPolicyService abacPolicyService;
    private final AbacPolicyBindingService abacPolicyBindingService;
    private final AbacPolicyRuleService abacPolicyRuleService;
    private final AbacResourceService abacResourceService;

    public AbacPolicyController(AbacPolicyService abacPolicyService,
                                AbacPolicyBindingService abacPolicyBindingService,
                                AbacPolicyRuleService abacPolicyRuleService,
                                AbacResourceService abacResourceService) {
        this.abacPolicyService = abacPolicyService;
        this.abacPolicyBindingService = abacPolicyBindingService;
        this.abacPolicyRuleService = abacPolicyRuleService;
        this.abacResourceService = abacResourceService;
    }

    @GetMapping
    public ResponseEntity<List<PolicyDetailsResponse>> list() {
        List<AbacPolicy> policies = abacPolicyService.list(
                new QueryWrapper<AbacPolicy>().orderByDesc("create_time").orderByDesc("id"));
        if (policies.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Long> policyIds = policies.stream().map(AbacPolicy::getId).toList();
        List<AbacPolicyBinding> bindings = abacPolicyBindingService.list(
                new QueryWrapper<AbacPolicyBinding>()
                        .in("policy_id", policyIds)
                        .orderByAsc("priority")
                        .orderByAsc("id"));
        List<AbacPolicyRule> rules = abacPolicyRuleService.list(
                new QueryWrapper<AbacPolicyRule>()
                        .in("policy_id", policyIds)
                        .orderByAsc("id"));
        Map<Long, List<AbacPolicyBinding>> bindingsByPolicy = bindings.stream()
                .collect(Collectors.groupingBy(AbacPolicyBinding::getPolicyId));
        Map<Long, List<AbacPolicyRule>> rulesByPolicy = rules.stream()
                .collect(Collectors.groupingBy(AbacPolicyRule::getPolicyId));

        List<PolicyDetailsResponse> response = policies.stream()
                .map(policy -> new PolicyDetailsResponse(
                        new PolicySummary(
                                policy.getId(),
                                policy.getPolicyCode(),
                                policy.getPolicyName(),
                                policy.getCombiningAlgorithm(),
                                policy.getEnabled(),
                                policy.getDescription(),
                                policy.getCreateTime(),
                                policy.getUpdateTime()),
                        bindingsByPolicy.getOrDefault(policy.getId(), List.of()).stream()
                                .map(binding -> new PolicyBindingSummary(
                                        binding.getId(),
                                        binding.getScopeType(),
                                        binding.getTenantId(),
                                        binding.getResourceId(),
                                        binding.getResourceType(),
                                        binding.getEnabled(),
                                        binding.getPriority()))
                                .toList(),
                        rulesByPolicy.getOrDefault(policy.getId(), List.of()).stream()
                                .map(rule -> new PolicyRuleSummary(
                                        rule.getId(),
                                        rule.getRuleCode(),
                                        rule.getRuleName(),
                                        rule.getEffect(),
                                        rule.getEnabled(),
                                        rule.getConditionStr(),
                                        rule.getExpression(),
                                        rule.getDescription()))
                                .toList()))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{policyId}/config")
    public ResponseEntity<?> generateConfig(@PathVariable("policyId") Long policyId) {
        if (policyId == null || policyId <= 0) {
            return ResponseEntity.badRequest().body(new ApiError("policyId must be positive"));
        }

        AbacPolicy policy = abacPolicyService.getById(policyId);
        if (policy == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiError("Policy not found"));
        }
        if (Boolean.FALSE.equals(policy.getEnabled())) {
            return ResponseEntity.badRequest()
                    .body(new ApiError("A disabled policy cannot generate an active config"));
        }

        List<AbacPolicyBinding> bindings = abacPolicyBindingService.list(
                new QueryWrapper<AbacPolicyBinding>()
                        .eq("policy_id", policyId)
                        .orderByAsc("priority")
                        .orderByAsc("id"));
        if (bindings.size() != 1
                || !"resource".equals(PolicyRuleSupport.normalize(bindings.get(0).getScopeType()))
                || bindings.get(0).getResourceId() == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiError("Policy config generation requires exactly one resource binding"));
        }
        AbacPolicyBinding binding = bindings.get(0);
        if (Boolean.FALSE.equals(binding.getEnabled())) {
            return ResponseEntity.badRequest()
                    .body(new ApiError("A disabled resource binding cannot generate an active config"));
        }

        AbacResource resource = abacResourceService.getById(binding.getResourceId());
        if (resource == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiError("The policy resource was not found"));
        }

        List<AbacPolicyRule> rules = abacPolicyRuleService.list(
                new QueryWrapper<AbacPolicyRule>()
                        .eq("policy_id", policyId)
                        .orderByAsc("id"));
        List<AbacPolicyRule> enabledRules = rules.stream()
                .filter(rule -> !Boolean.FALSE.equals(rule.getEnabled()))
                .toList();
        if (enabledRules.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiError("Policy has no enabled rules to generate"));
        }

        if (Boolean.TRUE.equals(resource.getExternal())) {
            List<ResourcePathNode> resourcePath;
            try {
                resourcePath = trinoResourcePath(resource);
            } catch (IllegalArgumentException exception) {
                return ResponseEntity.badRequest().body(new ApiError(exception.getMessage()));
            }
            for (AbacPolicyRule rule : enabledRules) {
                String effect = PolicyRuleSupport.normalize(rule.getEffect());
                if (!"column_masking".equals(effect) && !"row_filtering".equals(effect)) {
                    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                            .body(new ApiError(
                                    "Config generation for external resources does not support effect: "
                                            + rule.getEffect()));
                }
                if (!StringUtils.hasText(rule.getExpression())) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ApiError("An enabled external policy rule has no expression"));
                }
                if ("column_masking".equals(effect) && !StringUtils.hasText(rule.getConditionStr())) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ApiError("An enabled column masking rule has no condition"));
                }
            }
            String config = enabledRules.stream()
                    .map(rule -> wrapTrinoRule(rule, resourcePath))
                    .collect(Collectors.joining("\n\n"));
            return ResponseEntity.ok(new PolicyConfigResponse(
                    policy.getId(),
                    policy.getPolicyCode(),
                    resource.getId(),
                    resource.getResourceType(),
                    config));
        }
        if (!Boolean.FALSE.equals(resource.getExternal())) {
            return ResponseEntity.badRequest()
                    .body(new ApiError("The policy resource external flag is not configured"));
        }

        for (AbacPolicyRule rule : enabledRules) {
            if (!"permit".equals(PolicyRuleSupport.normalize(rule.getEffect()))) {
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                        .body(new ApiError(
                                "Config generation does not support enabled effect: " + rule.getEffect()));
            }
            if (!StringUtils.hasText(rule.getConditionStr())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiError("An enabled policy rule has no condition"));
            }
        }

        String config = enabledRules.stream()
                .map(rule -> wrapCondition(rule.getConditionStr()))
                .collect(Collectors.joining("\n\n"));
        return ResponseEntity.ok(new PolicyConfigResponse(
                policy.getId(),
                policy.getPolicyCode(),
                resource.getId(),
                resource.getResourceType(),
                config));
    }

    private List<ResourcePathNode> trinoResourcePath(AbacResource resource) {
        List<ResourcePathNode> path = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        AbacResource current = resource;
        while (current != null) {
            if (current.getId() == null || !visited.add(current.getId())) {
                throw new IllegalArgumentException("The Trino resource hierarchy is invalid");
            }
            String resourceType = PolicyRuleSupport.normalize(current.getResourceType());
            if ("trino_connection".equals(resourceType)) {
                if (current.getParentResourceId() != null
                        || !Boolean.TRUE.equals(current.getExternal())
                        || !StringUtils.hasText(current.getResourceName())) {
                    throw new IllegalArgumentException("The Trino resource hierarchy is invalid");
                }
                Collections.reverse(path);
                validateTrinoResourcePath(path);
                return path;
            }
            if (!TRINO_RESOURCE_TYPES.contains(resourceType)
                    || !Boolean.TRUE.equals(current.getExternal())
                    || !StringUtils.hasText(current.getResourceName())
                    || current.getParentResourceId() == null) {
                throw new IllegalArgumentException("The Trino resource hierarchy is invalid");
            }
            path.add(new ResourcePathNode(resourceType, current.getResourceName()));
            current = abacResourceService.getById(current.getParentResourceId());
        }
        throw new IllegalArgumentException("The Trino resource hierarchy is invalid");
    }

    private static void validateTrinoResourcePath(List<ResourcePathNode> path) {
        String parentType = "trino_connection";
        for (ResourcePathNode node : path) {
            boolean valid = switch (node.type()) {
                case "trino_catalog" -> "trino_connection".equals(parentType);
                case "trino_schema" -> "trino_catalog".equals(parentType);
                case "trino_table" -> "trino_schema".equals(parentType);
                case "trino_field" -> "trino_table".equals(parentType);
                default -> false;
            };
            if (!valid) {
                throw new IllegalArgumentException("The Trino resource hierarchy is invalid");
            }
            parentType = node.type();
        }
    }

    private static String wrapTrinoRule(AbacPolicyRule rule, List<ResourcePathNode> resourcePath) {
        String effect = PolicyRuleSupport.normalize(rule.getEffect());
        String targetType = "column_masking".equals(effect) ? "column" : "table";
        String ruleHead = "column_masking".equals(effect) ? "columnMask :=" : "rowFilters contains";
        StringBuilder config = new StringBuilder(ruleHead)
                .append(" {\"expression\": \"")
                .append(rule.getExpression())
                .append("\"} if {\n");
        if ("column_masking".equals(effect)) {
            appendIndentedLines(config, rule.getConditionStr());
            config.append("    column := input.action.resource.column\n");
        } else {
            config.append("    table := input.action.resource.table\n");
        }
        for (ResourcePathNode node : resourcePath) {
            String attribute = switch (node.type()) {
                case "trino_catalog" -> "catalogName";
                case "trino_schema" -> "schemaName";
                case "trino_table" -> "tableName";
                case "trino_field" -> "columnName";
                default -> null;
            };
            if (attribute != null && (!"trino_field".equals(node.type())
                    || "column_masking".equals(effect))) {
                config.append("    ")
                        .append(targetType)
                        .append('.')
                        .append(attribute)
                        .append(" == ")
                        .append(regoString(node.name()))
                        .append('\n');
            }
        }
        return config.append('}').toString();
    }

    private static void appendIndentedLines(StringBuilder output, String condition) {
        condition.strip().lines()
                .map(line -> "    " + line.stripTrailing())
                .forEach(line -> output.append(line).append('\n'));
    }

    private static String regoString(String value) {
        StringBuilder quoted = new StringBuilder("\"");
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> quoted.append("\\\"");
                case '\\' -> quoted.append("\\\\");
                case '\b' -> quoted.append("\\b");
                case '\f' -> quoted.append("\\f");
                case '\n' -> quoted.append("\\n");
                case '\r' -> quoted.append("\\r");
                case '\t' -> quoted.append("\\t");
                default -> {
                    if (character < 0x20) {
                        quoted.append(String.format("\\u%04x", (int) character));
                    } else {
                        quoted.append(character);
                    }
                }
            }
        }
        return quoted.append('"').toString();
    }

    @DeleteMapping("/{policyId}")
    public ResponseEntity<?> delete(@PathVariable("policyId") Long policyId) {
        if (policyId == null || policyId <= 0) {
            return ResponseEntity.badRequest().body(new ApiError("policyId must be positive"));
        }
        if (abacPolicyService.getById(policyId) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiError("Policy not found"));
        }
        try {
            abacPolicyService.deletePolicyBundle(policyId);
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError(exception.getMessage()));
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody(required = false) CreatePolicyRequest request) {
        String validationError = validate(request);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(new ApiError(validationError));
        }

        AbacResource resource = abacResourceService.getById(request.resourceId());
        if (resource == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("Resource not found"));
        }

        long now = Instant.now().toEpochMilli();
        AbacPolicy policy = new AbacPolicy();
        policy.setConfigId(DEFAULT_CONFIG_ID);
        policy.setPolicyCode("policy_" + UUID.randomUUID().toString().replace("-", ""));
        policy.setPolicyName(request.policyName().trim());
        policy.setCombiningAlgorithm(combiningAlgorithm(request.combiningAlgorithm()));
        policy.setEnabled(request.enabled() == null || request.enabled());
        policy.setDescription(request.description());
        policy.setCreateTime(now);
        policy.setUpdateTime(now);

        AbacPolicyBinding binding = new AbacPolicyBinding();
        binding.setScopeType("resource");
        binding.setTenantId(tenantIdFromOwner(resource.getOwner()));
        binding.setResourceId(resource.getId());
        binding.setResourceType(resource.getResourceType());
        binding.setEnabled(policy.getEnabled());
        binding.setPriority(100);
        binding.setCreateTime(now);
        binding.setUpdateTime(now);

        List<AbacPolicyRule> rules = new ArrayList<>();
        for (CreatePolicyRuleRequest ruleRequest : request.rules()) {
            AbacPolicyRule rule = new AbacPolicyRule();
            rule.setRuleCode("rule_" + UUID.randomUUID().toString().replace("-", ""));
            rule.setRuleName(ruleRequest.ruleName().trim());
            rule.setEffect(PolicyRuleSupport.normalize(ruleRequest.effect()));
            rule.setEnabled(ruleRequest.enabled() == null || ruleRequest.enabled());
            rule.setConditionStr(PolicyRuleSupport.compileConditions(ruleRequest.conditions()));
            rule.setExpression(StringUtils.hasText(ruleRequest.expression())
                    ? ruleRequest.expression().trim() : null);
            rule.setDescription(ruleRequest.description());
            rule.setCreateTime(now);
            rule.setUpdateTime(now);
            rules.add(rule);
        }

        try {
            abacPolicyService.createPolicyBundle(policy, binding, rules);
        } catch (DuplicateKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiError("A generated policy or rule code already exists"));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError(exception.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PolicySubmissionResponse(policy, binding, rules));
    }

    private static String validate(CreatePolicyRequest request) {
        if (request == null) {
            return "Request body is required";
        }
        if (!StringUtils.hasText(request.policyName()) || request.policyName().trim().length() > 256) {
            return "policyName is required and must be at most 256 characters";
        }
        if (request.resourceId() == null || request.resourceId() <= 0) {
            return "resourceId must be positive";
        }
        if (request.rules() == null || request.rules().isEmpty()) {
            return "At least one rule is required";
        }
        if (StringUtils.hasText(request.combiningAlgorithm())
                && !COMBINING_ALGORITHMS.contains(PolicyRuleSupport.normalize(request.combiningAlgorithm()))) {
            return "combiningAlgorithm must be deny_overrides, permit_overrides, or first_applicable";
        }
        for (int index = 0; index < request.rules().size(); index++) {
            CreatePolicyRuleRequest rule = request.rules().get(index);
            if (rule == null) {
                return "rules[" + index + "] is required";
            }
            String ruleError = PolicyRuleSupport.validate(
                    rule.ruleName(), rule.effect(), rule.conditions(), rule.expression());
            if (ruleError != null) {
                return "rules[" + index + "]: " + ruleError;
            }
        }
        return null;
    }

    private static String combiningAlgorithm(String value) {
        return StringUtils.hasText(value) ? PolicyRuleSupport.normalize(value) : "deny_overrides";
    }

    private static Long tenantIdFromOwner(String owner) {
        if (!StringUtils.hasText(owner)) {
            return null;
        }
        try {
            long tenantId = Long.parseLong(owner);
            return tenantId > 0 ? tenantId : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static String wrapCondition(String condition) {
        String indentedCondition = condition.strip().lines()
                .map(line -> "    " + line.stripTrailing())
                .collect(Collectors.joining("\n"));
        return "allow if {\n" + indentedCondition + "\n}";
    }

    public record CreatePolicyRequest(String policyName,
                                      String combiningAlgorithm,
                                      Boolean enabled,
                                      String description,
                                      Long resourceId,
                                      List<CreatePolicyRuleRequest> rules) {
    }

    public record CreatePolicyRuleRequest(String ruleName,
                                          String effect,
                                          Boolean enabled,
                                          List<AbacPolicyRuleController.ConditionRequest> conditions,
                                          String expression,
                                          String description) {
    }

    public record PolicySubmissionResponse(AbacPolicy policy,
                                           AbacPolicyBinding binding,
                                           List<AbacPolicyRule> rules) {
    }

    public record PolicyConfigResponse(Long policyId,
                                       String policyCode,
                                       Long resourceId,
                                       String resourceType,
                                       String config) {
    }

    private record ResourcePathNode(String type, String name) {
    }

    public record PolicyDetailsResponse(PolicySummary policy,
                                        List<PolicyBindingSummary> bindings,
                                        List<PolicyRuleSummary> rules) {
    }

    public record PolicySummary(Long id,
                                String policyCode,
                                String policyName,
                                String combiningAlgorithm,
                                Boolean enabled,
                                String description,
                                Long createTime,
                                Long updateTime) {
    }

    public record PolicyBindingSummary(Long id,
                                       String scopeType,
                                       Long tenantId,
                                       Long resourceId,
                                       String resourceType,
                                       Boolean enabled,
                                       Integer priority) {
    }

    public record PolicyRuleSummary(Long id,
                                    String ruleCode,
                                    String ruleName,
                                    String effect,
                                    Boolean enabled,
                                    String conditionStr,
                                    String expression,
                                    String description) {
    }

    public record ApiError(String message) {
    }
}
