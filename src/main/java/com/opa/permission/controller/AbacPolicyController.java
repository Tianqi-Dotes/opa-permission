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
