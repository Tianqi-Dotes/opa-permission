package com.opa.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.opa.permission.model.AbacResource;
import com.opa.permission.model.AttributeDefinition;
import com.opa.permission.model.PermissionDefaultAttributeWrite;
import com.opa.permission.model.ResourceAttributeValue;
import com.opa.permission.model.SubjectAttributeValue;
import com.opa.permission.service.AttributeDefinitionService;
import com.opa.permission.service.DefaultAttributeInjectionException;
import com.opa.permission.service.PermissionDefaultAttributeInjectionService;
import com.opa.permission.service.PermissionDefaultAttributeWriteService;
import com.opa.permission.service.ResourceAttributeValueService;
import com.opa.permission.service.SubjectAttributeValueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
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

    public PermissionDefaultAttributeInjectionServiceImpl(
            PermissionDefaultAttributeWriteService defaultAttributeWriteService,
            AttributeDefinitionService attributeDefinitionService,
            SubjectAttributeValueService subjectAttributeValueService,
            ResourceAttributeValueService resourceAttributeValueService) {
        this.defaultAttributeWriteService = defaultAttributeWriteService;
        this.attributeDefinitionService = attributeDefinitionService;
        this.subjectAttributeValueService = subjectAttributeValueService;
        this.resourceAttributeValueService = resourceAttributeValueService;
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
        if (jwt == null) {
            throw new DefaultAttributeInjectionException(
                    "A JWT is required to inject default attributes for permission " + permissionId);
        }

        List<PreparedWrite> preparedWrites = writes.stream()
                .map(write -> prepareWrite(permissionId, write, jwt))
                .filter(Objects::nonNull)
                .toList();
        if (preparedWrites.isEmpty()) {
            return;
        }

        boolean needsSubject = preparedWrites.stream()
                .anyMatch(write -> "subject".equals(write.configuration().getTargetType()));
        boolean needsResource = preparedWrites.stream()
                .anyMatch(write -> "resource".equals(write.configuration().getTargetType()));
        for (PreparedWrite prepared : preparedWrites) {
            String targetType = prepared.configuration().getTargetType();
            if (!"subject".equals(targetType) && !"resource".equals(targetType)) {
                throw new DefaultAttributeInjectionException(
                        "Unsupported attribute target type: " + targetType);
            }
        }
        Long subjectId = needsSubject ? longClaim(jwt, "user_id") : null;
        Long resourceId = needsResource ? resourceId(responseBody) : null;
        if (needsSubject && subjectId == null) {
            throw new DefaultAttributeInjectionException(
                    "The JWT is missing the user_id claim required by permission " + permissionId);
        }
        if (needsResource && resourceId == null) {
            throw new DefaultAttributeInjectionException(
                    "The successful response for permission " + permissionId
                            + " did not contain a resource ID");
        }

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

    private PreparedWrite prepareWrite(Long permissionId, PermissionDefaultAttributeWrite write, Jwt jwt) {
        if (write.getDefaultValueText() != null) {
            List<AttributeValue> values = resolveText(permissionId, write, jwt);
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
                                             Jwt jwt) {
        String template = write.getDefaultValueText();
        Matcher wholePlaceholder = PLACEHOLDER_PATTERN.matcher(template);
        if (wholePlaceholder.matches()) {
            Object claim = claimValue(jwt, wholePlaceholder.group(1));
            if (claim == null) {
                LOGGER.warn("Skipping default attribute write {} for permission {}: JWT claim '{}' is absent",
                        write.getId(), permissionId, wholePlaceholder.group(1));
                return List.of();
            }
            if (claim instanceof Collection<?> values) {
                List<AttributeValue> resolvedValues = values.stream()
                        .filter(Objects::nonNull)
                        .map(value -> new AttributeValue(String.valueOf(value), null))
                        .toList();
                if (resolvedValues.isEmpty()) {
                    LOGGER.warn("Skipping default attribute write {} for permission {}: JWT claim '{}' is empty",
                            write.getId(), permissionId, wholePlaceholder.group(1));
                }
                return resolvedValues;
            }
            return List.of(new AttributeValue(String.valueOf(claim), null));
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer resolved = new StringBuffer();
        while (matcher.find()) {
            Object claim = claimValue(jwt, matcher.group(1));
            if (claim == null) {
                LOGGER.warn("Skipping default attribute write {} for permission {}: JWT claim '{}' is absent",
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

    private Object claimValue(Jwt jwt, String claimName) {
        Object value = jwt.getClaims().get(claimName);
        if (value == null && "role".equals(claimName)) {
            value = jwt.getClaims().get("roles");
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

    private Long longClaim(Jwt jwt, String claimName) {
        Object claim = jwt.getClaims().get(claimName);
        return claim instanceof Number number ? number.longValue() : null;
    }

    private long now() {
        return Instant.now().toEpochMilli();
    }

    private record AttributeValue(String text, Object json) {
    }

    private record PreparedWrite(PermissionDefaultAttributeWrite configuration, List<AttributeValue> values) {
    }
}
