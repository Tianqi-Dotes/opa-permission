package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.opa.permission.model.AbacResource;
import com.opa.permission.model.AttributeDefinition;
import com.opa.permission.model.PermissionDefaultAttributeWrite;
import com.opa.permission.model.ResourceAttributeValue;
import com.opa.permission.model.Role;
import com.opa.permission.model.SubjectIdProvider;
import com.opa.permission.model.SubjectAttributeValue;
import com.opa.permission.model.UserAccount;
import com.opa.permission.model.UserRole;
import com.opa.permission.service.AttributeDefinitionService;
import com.opa.permission.service.DefaultAttributeInjectionException;
import com.opa.permission.service.PermissionDefaultAttributeInjectionService;
import com.opa.permission.service.PermissionDefaultAttributeWriteService;
import com.opa.permission.service.ResourceAttributeValueService;
import com.opa.permission.service.RoleService;
import com.opa.permission.service.SubjectAttributeValueService;
import com.opa.permission.service.UserAccountService;
import com.opa.permission.service.UserRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PermissionDefaultAttributeInjectionServiceImpl
        implements PermissionDefaultAttributeInjectionService {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(PermissionDefaultAttributeInjectionServiceImpl.class);
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([A-Za-z0-9_]+)}}");

    private final PermissionDefaultAttributeWriteService defaultAttributeWriteService;
    private final AttributeDefinitionService attributeDefinitionService;
    private final SubjectAttributeValueService subjectAttributeValueService;
    private final ResourceAttributeValueService resourceAttributeValueService;
    private final UserAccountService userAccountService;
    private final UserRoleService userRoleService;
    private final RoleService roleService;

    public PermissionDefaultAttributeInjectionServiceImpl(
            PermissionDefaultAttributeWriteService defaultAttributeWriteService,
            AttributeDefinitionService attributeDefinitionService,
            SubjectAttributeValueService subjectAttributeValueService,
            ResourceAttributeValueService resourceAttributeValueService,
            UserAccountService userAccountService,
            UserRoleService userRoleService,
            RoleService roleService) {
        this.defaultAttributeWriteService = defaultAttributeWriteService;
        this.attributeDefinitionService = attributeDefinitionService;
        this.subjectAttributeValueService = subjectAttributeValueService;
        this.resourceAttributeValueService = resourceAttributeValueService;
        this.userAccountService = userAccountService;
        this.userRoleService = userRoleService;
        this.roleService = roleService;
    }

    @Override
    @Transactional
    public void injectForPermission(Long permissionId, Jwt jwt, Object responseBody) {
        List<PermissionDefaultAttributeWrite> writes = defaultAttributeWriteService.list(
                new LambdaQueryWrapper<PermissionDefaultAttributeWrite>()
                        .eq(PermissionDefaultAttributeWrite::getPermissionId, permissionId)
                        .eq(PermissionDefaultAttributeWrite::getShouldInject, true)
                        .orderByAsc(PermissionDefaultAttributeWrite::getFromType)
                        .orderByAsc(PermissionDefaultAttributeWrite::getId));
        if (writes.isEmpty()) {
            return;
        }

        boolean needsSubject = false;
        boolean needsResource = false;
        for (PermissionDefaultAttributeWrite write : writes) {
            String targetType = write.getTargetType();
            if (!"subject".equals(targetType) && !"resource".equals(targetType)) {
                throw new DefaultAttributeInjectionException(
                        "Unsupported attribute target type: " + targetType);
            }
            needsSubject |= "subject".equals(targetType);
            needsResource |= "resource".equals(targetType);
        }

        if (needsResource && jwt == null) {
            throw new DefaultAttributeInjectionException(
                    "A JWT is required to inject resource defaults for permission " + permissionId);
        }
        Long subjectId = needsSubject ? subjectId(responseBody) : null;
        Long resourceId = needsResource ? resourceId(responseBody) : null;
        if (needsSubject && subjectId == null) {
            throw new DefaultAttributeInjectionException(
                    "The successful response for permission " + permissionId
                            + " did not provide the target subject ID");
        }
        if (needsResource && resourceId == null) {
            throw new DefaultAttributeInjectionException(
                    "The successful response for permission " + permissionId
                            + " did not contain a resource ID");
        }

        Map<String, Object> subjectContext =
                needsSubject ? subjectAttributeContext(permissionId, subjectId) : Map.of();
        Map<String, Object> actorContext = jwt == null ? Map.of() : jwt.getClaims();
        List<PreparedWrite> preparedWrites = writes.stream()
                .map(write -> prepareWrite(
                        permissionId,
                        write,
                        "subject".equals(write.getTargetType()) ? subjectContext : actorContext))
                .filter(Objects::nonNull)
                .toList();

        for (PreparedWrite prepared : preparedWrites) {
            PermissionDefaultAttributeWrite configuration = prepared.configuration();
            AttributeDefinition definition = attributeDefinitionService.getById(configuration.getAttributeId());
            if (definition == null) {
                throw new DefaultAttributeInjectionException(
                        "Attribute " + configuration.getAttributeId() + " configured for permission "
                                + permissionId + " does not exist");
            }
            String expectedCategory = "subject".equals(configuration.getTargetType()) ? "subject" : "resource";
            if (!expectedCategory.equalsIgnoreCase(definition.getCategory())) {
                throw new DefaultAttributeInjectionException(
                        "Attribute " + configuration.getAttributeId() + " is not a "
                                + expectedCategory + " attribute");
            }

            boolean multiValued = Boolean.TRUE.equals(definition.getMultiValued());
            if ("subject".equals(configuration.getTargetType())) {
                injectSubjectValues(subjectId, configuration.getAttributeId(), prepared.values(), multiValued);
            } else if ("resource".equals(configuration.getTargetType())) {
                injectResourceValues(resourceId, configuration.getAttributeId(), prepared.values(), multiValued);
            } else {
                throw new DefaultAttributeInjectionException(
                        "Unsupported attribute target type: " + configuration.getTargetType());
            }
        }
    }

    private PreparedWrite prepareWrite(Long permissionId,
                                       PermissionDefaultAttributeWrite write,
                                       Map<String, Object> context) {
        if (write.getDefaultValueText() != null) {
            List<AttributeValue> values = resolveText(permissionId, write, context);
            return values.isEmpty() ? null : new PreparedWrite(write, values);
        }
        if (write.getDefaultValueJson() != null) {
            return new PreparedWrite(write, List.of(new AttributeValue(null, write.getDefaultValueJson())));
        }
        throw new DefaultAttributeInjectionException(
                "Default attribute configuration " + write.getId() + " has no value");
    }

    private List<AttributeValue> resolveText(Long permissionId,
                                             PermissionDefaultAttributeWrite write,
                                             Map<String, Object> context) {
        String template = write.getDefaultValueText();
        Matcher wholePlaceholder = PLACEHOLDER_PATTERN.matcher(template);
        if (wholePlaceholder.matches()) {
            Object claim = claimValue(context, wholePlaceholder.group(1));
            if (claim == null) {
                LOGGER.warn("Skipping default attribute write {} for permission {}: template value '{}' is absent",
                        write.getId(), permissionId, wholePlaceholder.group(1));
                return List.of();
            }
            if (claim instanceof Collection<?> values) {
                List<AttributeValue> resolvedValues = values.stream()
                        .filter(Objects::nonNull)
                        .map(value -> new AttributeValue(String.valueOf(value), null))
                        .toList();
                if (resolvedValues.isEmpty()) {
                    LOGGER.warn("Skipping default attribute write {} for permission {}: template value '{}' is empty",
                            write.getId(), permissionId, wholePlaceholder.group(1));
                }
                return resolvedValues;
            }
            return List.of(new AttributeValue(String.valueOf(claim), null));
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer resolved = new StringBuffer();
        while (matcher.find()) {
            Object claim = claimValue(context, matcher.group(1));
            if (claim == null) {
                LOGGER.warn("Skipping default attribute write {} for permission {}: template value '{}' is absent",
                        write.getId(), permissionId, matcher.group(1));
                return List.of();
            }
            String replacement = claim instanceof Collection<?> values
                    ? values.stream().filter(Objects::nonNull).map(String::valueOf)
                    .collect(java.util.stream.Collectors.joining(","))
                    : String.valueOf(claim);
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(resolved);
        return List.of(new AttributeValue(resolved.toString(), null));
    }

    private Object claimValue(Map<String, Object> context, String claimName) {
        Object value = context.get(claimName);
        if (value == null && "role".equals(claimName)) {
            value = context.get("roles");
        }
        return value;
    }

    private void injectSubjectValues(Long subjectId,
                                     Long attributeId,
                                     List<AttributeValue> values,
                                     boolean multiValued) {
        List<SubjectAttributeValue> existingValues = subjectAttributeValueService.list(
                new LambdaQueryWrapper<SubjectAttributeValue>()
                        .eq(SubjectAttributeValue::getSubjectId, subjectId)
                        .eq(SubjectAttributeValue::getAttributeId, attributeId)
                        .orderByAsc(SubjectAttributeValue::getId));
        if (!multiValued && existingValues.size() > 1) {
            throw new DefaultAttributeInjectionException(
                    "Multiple values already exist for single-valued subject attribute " + attributeId);
        }
        if (!multiValued && values.size() > 1) {
            throw new DefaultAttributeInjectionException(
                    "Multiple default values configured for single-valued subject attribute " + attributeId);
        }

        for (AttributeValue value : values) {
            SubjectAttributeValue existing = existingValues.stream()
                    .filter(item -> sameValue(item.getValueText(), item.getValueJson(), value))
                    .findFirst()
                    .orElse(null);
            if (existing != null) {
                continue;
            }
            if (!multiValued && !existingValues.isEmpty()) {
                existing = existingValues.get(0);
                setValue(existing, value);
                existing.setUpdateTime(now());
                if (!subjectAttributeValueService.updateById(existing)) {
                    throw new DefaultAttributeInjectionException(
                            "Unable to update subject attribute " + attributeId);
                }
                existingValues.set(0, existing);
                continue;
            }
            SubjectAttributeValue newValue = new SubjectAttributeValue();
            newValue.setSubjectId(subjectId);
            newValue.setAttributeId(attributeId);
            setValue(newValue, value);
            newValue.setCreateTime(now());
            newValue.setUpdateTime(now());
            if (!subjectAttributeValueService.save(newValue)) {
                throw new DefaultAttributeInjectionException(
                        "Unable to inject subject attribute " + attributeId);
            }
            existingValues.add(newValue);
        }
    }

    private void injectResourceValues(Long resourceId,
                                      Long attributeId,
                                      List<AttributeValue> values,
                                      boolean multiValued) {
        List<ResourceAttributeValue> existingValues = resourceAttributeValueService.list(
                new LambdaQueryWrapper<ResourceAttributeValue>()
                        .eq(ResourceAttributeValue::getResourceId, resourceId)
                        .eq(ResourceAttributeValue::getAttributeId, attributeId)
                        .orderByAsc(ResourceAttributeValue::getId));
        if (!multiValued && existingValues.size() > 1) {
            throw new DefaultAttributeInjectionException(
                    "Multiple values already exist for single-valued resource attribute " + attributeId);
        }
        if (!multiValued && values.size() > 1) {
            throw new DefaultAttributeInjectionException(
                    "Multiple default values configured for single-valued resource attribute " + attributeId);
        }

        for (AttributeValue value : values) {
            ResourceAttributeValue existing = existingValues.stream()
                    .filter(item -> sameValue(item.getValueText(), item.getValueJson(), value))
                    .findFirst()
                    .orElse(null);
            if (existing != null) {
                continue;
            }
            if (!multiValued && !existingValues.isEmpty()) {
                existing = existingValues.get(0);
                setValue(existing, value);
                existing.setUpdateTime(now());
                if (!resourceAttributeValueService.updateById(existing)) {
                    throw new DefaultAttributeInjectionException(
                            "Unable to update resource attribute " + attributeId);
                }
                existingValues.set(0, existing);
                continue;
            }
            ResourceAttributeValue newValue = new ResourceAttributeValue();
            newValue.setResourceId(resourceId);
            newValue.setAttributeId(attributeId);
            setValue(newValue, value);
            newValue.setCreateTime(now());
            newValue.setUpdateTime(now());
            if (!resourceAttributeValueService.save(newValue)) {
                throw new DefaultAttributeInjectionException(
                        "Unable to inject resource attribute " + attributeId);
            }
            existingValues.add(newValue);
        }
    }

    private void setValue(SubjectAttributeValue target, AttributeValue value) {
        target.setValueText(value.text());
        target.setValueJson(value.json());
    }

    private void setValue(ResourceAttributeValue target, AttributeValue value) {
        target.setValueText(value.text());
        target.setValueJson(value.json());
    }

    private boolean sameValue(String text, Object json, AttributeValue value) {
        return Objects.equals(text, value.text()) && Objects.equals(json, value.json());
    }

    private Long resourceId(Object responseBody) {
        if (responseBody instanceof AbacResource resource) {
            return resource.getId();
        }
        return null;
    }

    private Long subjectId(Object responseBody) {
        return responseBody instanceof SubjectIdProvider provider ? provider.subjectId() : null;
    }

    private Map<String, Object> subjectAttributeContext(Long permissionId, Long subjectId) {
        UserAccount subject = userAccountService.getById(subjectId);
        if (subject == null) {
            throw new DefaultAttributeInjectionException(
                    "Target subject " + subjectId + " for permission " + permissionId + " was not found");
        }
        if (subject.getTenantId() == null) {
            throw new DefaultAttributeInjectionException(
                    "Target subject " + subjectId + " has no tenant ID");
        }

        Map<String, Object> context = new HashMap<>();
        context.put("tenant_id", subject.getTenantId());
        if (StringUtils.hasText(subject.getGroupName())) {
            context.put("group_name", subject.getGroupName());
        }

        List<Long> roleIds = userRoleService.list(new LambdaQueryWrapper<UserRole>()
                        .select(UserRole::getRoleId)
                        .eq(UserRole::getUserId, subject.getUserId())
                        .eq(UserRole::getTenantId, subject.getTenantId()))
                .stream()
                .map(UserRole::getRoleId)
                .distinct()
                .toList();
        List<String> roles = (roleIds.isEmpty() ? List.<Role>of() : roleService.listByIds(roleIds)).stream()
                .filter(role -> "active".equalsIgnoreCase(role.getStatus()))
                .map(Role::getRoleName)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (!roles.isEmpty()) {
            context.put("roles", roles);
        }
        return context;
    }

    private long now() {
        return Instant.now().toEpochMilli();
    }

    private record AttributeValue(String text, Object json) {
    }

    private record PreparedWrite(PermissionDefaultAttributeWrite configuration, List<AttributeValue> values) {
    }
}
