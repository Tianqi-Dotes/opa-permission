package com.opa.permission.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.opa.permission.model.AbacResource;
import com.opa.permission.model.AttributeDefinition;
import com.opa.permission.model.ResourceAttributeValue;
import com.opa.permission.service.AbacResourceService;
import com.opa.permission.service.AttributeDefinitionService;
import com.opa.permission.service.ResourceAttributeValueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/api/abac/resource-attributes")
public class ResourceAttributeValueController {
    private final ResourceAttributeValueService resourceAttributeValueService;
    private final AttributeDefinitionService attributeDefinitionService;
    private final AbacResourceService abacResourceService;
    private final AttributeValueValidator valueValidator;

    public ResourceAttributeValueController(ResourceAttributeValueService resourceAttributeValueService,
                                            AttributeDefinitionService attributeDefinitionService,
                                            AbacResourceService abacResourceService,
                                            AttributeValueValidator valueValidator) {
        this.resourceAttributeValueService = resourceAttributeValueService;
        this.attributeDefinitionService = attributeDefinitionService;
        this.abacResourceService = abacResourceService;
        this.valueValidator = valueValidator;
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(value = "resourceId", required = false) Long resourceId,
            @RequestParam(value = "attributeId", required = false) Long attributeId) {
        QueryWrapper<ResourceAttributeValue> query = new QueryWrapper<>();
        if (resourceId != null) {
            query.eq("resource_id", resourceId);
        }
        if (attributeId != null) {
            query.eq("attribute_id", attributeId);
        }
        query.orderByAsc("resource_id", "attribute_id", "id");
        List<ResourceAttributeValue> values = resourceAttributeValueService.list(query);
        if (values.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        Set<Long> resourceIds = new HashSet<>();
        values.stream()
                .map(ResourceAttributeValue::getResourceId)
                .filter(Objects::nonNull)
                .forEach(resourceIds::add);
        Map<Long, String> resourceCodes = new HashMap<>();
        if (!resourceIds.isEmpty()) {
            List<AbacResource> resources = abacResourceService.list(
                    new QueryWrapper<AbacResource>().in("id", resourceIds));
            for (AbacResource resource : resources) {
                resourceCodes.put(resource.getId(), resource.getResourceCode());
            }
        }

        return ResponseEntity.ok(values.stream()
                .map(value -> new ResourceAttributeValueResponse(
                        value.getId(),
                        value.getResourceId(),
                        value.getAttributeId(),
                        resourceCodes.get(value.getResourceId()),
                        value.getValueText(),
                        value.getValueJson(),
                        value.getCreateTime(),
                        value.getUpdateTime()))
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Long id) {
        ResourceAttributeValue value = resourceAttributeValueService.getById(id);
        return value == null ? notFound() : ResponseEntity.ok(value);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody(required = false) ResourceAttributeValueRequest request) {
        String requestError = validateRequest(request);
        if (requestError != null) {
            return badRequest(requestError);
        }

        AbacResource resource = abacResourceService.getById(request.resourceId());
        if (resource == null) {
            return notFound("Resource not found");
        }
        AttributeDefinition definition = attributeDefinitionService.getById(request.attributeId());
        ResponseEntity<ApiError> definitionError = validateDefinition(definition);
        if (definitionError != null) {
            return definitionError;
        }

        AttributeValueValidator.ValidationResult valueResult =
                valueValidator.validate(definition, request.valueText());
        if (!valueResult.isValid()) {
            return badRequest(valueResult.error());
        }
        if (hasSingleValueConflict(request.resourceId(), request.attributeId(), null, definition)) {
            return conflict("A value already exists for this single-valued resource attribute");
        }

        long now = Instant.now().toEpochMilli();
        ResourceAttributeValue value = new ResourceAttributeValue();
        value.setResourceId(request.resourceId());
        value.setAttributeId(request.attributeId());
        value.setValueText(valueResult.valueText());
        value.setCreateTime(now);
        value.setUpdateTime(now);
        if (!resourceAttributeValueService.save(value)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("Resource attribute value was not created"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(value);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id,
                                    @RequestBody(required = false) ResourceAttributeValueRequest request) {
        String requestError = validateRequest(request);
        if (requestError != null) {
            return badRequest(requestError);
        }

        ResourceAttributeValue existing = resourceAttributeValueService.getById(id);
        if (existing == null) {
            return notFound();
        }
        if (abacResourceService.getById(request.resourceId()) == null) {
            return notFound("Resource not found");
        }
        AttributeDefinition definition = attributeDefinitionService.getById(request.attributeId());
        ResponseEntity<ApiError> definitionError = validateDefinition(definition);
        if (definitionError != null) {
            return definitionError;
        }

        AttributeValueValidator.ValidationResult valueResult =
                valueValidator.validate(definition, request.valueText());
        if (!valueResult.isValid()) {
            return badRequest(valueResult.error());
        }
        if (hasSingleValueConflict(request.resourceId(), request.attributeId(), id, definition)) {
            return conflict("A value already exists for this single-valued resource attribute");
        }

        existing.setResourceId(request.resourceId());
        existing.setAttributeId(request.attributeId());
        existing.setValueText(valueResult.valueText());
        existing.setUpdateTime(Instant.now().toEpochMilli());
        if (!resourceAttributeValueService.updateById(existing)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("Resource attribute value was not updated"));
        }
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        if (resourceAttributeValueService.getById(id) == null) {
            return notFound();
        }
        if (!resourceAttributeValueService.removeById(id)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("Resource attribute value was not deleted"));
        }
        return ResponseEntity.noContent().build();
    }

    private String validateRequest(ResourceAttributeValueRequest request) {
        if (request == null) {
            return "Request body is required";
        }
        if (request.resourceId() == null || request.resourceId() <= 0) {
            return "resourceId must be a positive integer";
        }
        if (request.attributeId() == null || request.attributeId() <= 0) {
            return "attributeId must be a positive integer";
        }
        if (request.valueText() == null) {
            return "valueText is required";
        }
        return null;
    }

    private ResponseEntity<ApiError> validateDefinition(AttributeDefinition definition) {
        if (definition == null) {
            return notFound("Attribute definition not found");
        }
        if (!"resource".equals(normalize(definition.getCategory()))) {
            return badRequest("attributeId must reference a resource attribute");
        }
        return null;
    }

    private boolean hasSingleValueConflict(Long resourceId, Long attributeId, Long excludedId,
                                           AttributeDefinition definition) {
        if (Boolean.TRUE.equals(definition.getMultiValued())) {
            return false;
        }
        QueryWrapper<ResourceAttributeValue> query = new QueryWrapper<>();
        query.eq("resource_id", resourceId).eq("attribute_id", attributeId);
        if (excludedId != null) {
            query.ne("id", excludedId);
        }
        return resourceAttributeValueService.count(query) > 0;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static ResponseEntity<ApiError> badRequest(String message) {
        return ResponseEntity.badRequest().body(new ApiError(message));
    }

    private static ResponseEntity<ApiError> notFound() {
        return notFound("Resource attribute value not found");
    }

    private static ResponseEntity<ApiError> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(message));
    }

    private static ResponseEntity<ApiError> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(message));
    }

    public record ResourceAttributeValueRequest(Long resourceId, Long attributeId, String valueText) {
    }

    public record ResourceAttributeValueResponse(Long id,
                                                 Long resourceId,
                                                 Long attributeId,
                                                 String resourceCode,
                                                 String valueText,
                                                 Object valueJson,
                                                 Long createTime,
                                                 Long updateTime) {
    }

    public record ApiError(String message) {
    }
}
