package com.opa.permission.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.opa.permission.model.AttributeDefinition;
import com.opa.permission.model.ResourceAttributeValue;
import com.opa.permission.model.SubjectAttributeValue;
import com.opa.permission.service.AttributeDefinitionService;
import com.opa.permission.service.ResourceAttributeValueService;
import com.opa.permission.service.SubjectAttributeValueService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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
import java.util.List;
import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/api/abac/attributes")
public class AttributeDefinitionController {
    private static final Set<String> CATEGORIES = Set.of("subject", "resource", "environment", "action");
    private static final Set<String> DATA_TYPES =
            Set.of("string", "number", "boolean", "date", "datetime", "json");

    private final AttributeDefinitionService attributeDefinitionService;
    private final SubjectAttributeValueService subjectAttributeValueService;
    private final ResourceAttributeValueService resourceAttributeValueService;

    public AttributeDefinitionController(AttributeDefinitionService attributeDefinitionService,
                                         SubjectAttributeValueService subjectAttributeValueService,
                                         ResourceAttributeValueService resourceAttributeValueService) {
        this.attributeDefinitionService = attributeDefinitionService;
        this.subjectAttributeValueService = subjectAttributeValueService;
        this.resourceAttributeValueService = resourceAttributeValueService;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(value = "category", required = false) String category) {
        String normalizedCategory = normalize(category);
        if (StringUtils.hasText(normalizedCategory) && !CATEGORIES.contains(normalizedCategory)) {
            return ResponseEntity.badRequest().body(new ApiError("category is not supported"));
        }

        QueryWrapper<AttributeDefinition> query = new QueryWrapper<>();
        if (StringUtils.hasText(normalizedCategory)) {
            query.eq("category", normalizedCategory);
        }
        query.orderByAsc("category", "name");
        return ResponseEntity.ok(attributeDefinitionService.list(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Long id) {
        AttributeDefinition attribute = attributeDefinitionService.getById(id);
        if (attribute == null) {
            return notFound();
        }
        return ResponseEntity.ok(attribute);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody(required = false) AttributeDefinitionRequest request) {
        String validationError = validate(request);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(new ApiError(validationError));
        }

        long now = Instant.now().toEpochMilli();
        AttributeDefinition attribute = toEntity(request);
        attribute.setCreateTime(now);
        attribute.setUpdateTime(now);

        try {
            if (!attributeDefinitionService.save(attribute)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiError("Attribute definition was not created"));
            }
        } catch (DuplicateKeyException exception) {
            return conflict();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(attribute);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id,
                                    @RequestBody(required = false) AttributeDefinitionRequest request) {
        String validationError = validate(request);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(new ApiError(validationError));
        }

        AttributeDefinition existing = attributeDefinitionService.getById(id);
        if (existing == null) {
            return notFound();
        }
        if (Boolean.TRUE.equals(existing.getIsDefault())) {
            return conflict("Default attribute definitions cannot be modified");
        }

        copyRequestFields(request, existing);
        existing.setUpdateTime(Instant.now().toEpochMilli());
        try {
            if (!attributeDefinitionService.updateById(existing)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiError("Attribute definition was not updated"));
            }
        } catch (DuplicateKeyException exception) {
            return conflict();
        }
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        AttributeDefinition existing = attributeDefinitionService.getById(id);
        if (existing == null) {
            return notFound();
        }
        if (Boolean.TRUE.equals(existing.getIsDefault())) {
            return conflict("Default attribute definitions cannot be deleted");
        }

        QueryWrapper<SubjectAttributeValue> subjectValues = new QueryWrapper<>();
        subjectValues.eq("attribute_id", id);
        QueryWrapper<ResourceAttributeValue> resourceValues = new QueryWrapper<>();
        resourceValues.eq("attribute_id", id);
        if (subjectAttributeValueService.count(subjectValues) > 0
                || resourceAttributeValueService.count(resourceValues) > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiError("Attribute definition is in use"));
        }

        if (!attributeDefinitionService.removeById(id)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("Attribute definition was not deleted"));
        }
        return ResponseEntity.noContent().build();
    }

    private static String validate(AttributeDefinitionRequest request) {
        if (request == null) {
            return "Request body is required";
        }
        if (!StringUtils.hasText(request.name()) || request.name().trim().length() > 128) {
            return "name is required and must be at most 128 characters";
        }
        String category = normalize(request.category());
        if (!CATEGORIES.contains(category)) {
            return "category must be subject, resource, environment, or action";
        }
        String dataType = normalize(request.dataType());
        if (!DATA_TYPES.contains(dataType)) {
            return "dataType must be string, number, boolean, date, datetime, or json";
        }
        return null;
    }

    private static AttributeDefinition toEntity(AttributeDefinitionRequest request) {
        AttributeDefinition attribute = new AttributeDefinition();
        copyRequestFields(request, attribute);
        attribute.setIsDefault(false);
        return attribute;
    }

    private static void copyRequestFields(AttributeDefinitionRequest request, AttributeDefinition target) {
        target.setName(request.name().trim());
        target.setCategory(normalize(request.category()));
        target.setDataType(normalize(request.dataType()));
        target.setMultiValued(Boolean.TRUE.equals(request.multiValued()));
        target.setDescription(request.description());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static ResponseEntity<ApiError> notFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("Attribute definition not found"));
    }

    private static ResponseEntity<ApiError> conflict() {
        return conflict("An attribute with this category and name already exists");
    }

    private static ResponseEntity<ApiError> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(message));
    }

    public record AttributeDefinitionRequest(String name,
                                             String category,
                                             String dataType,
                                             Boolean multiValued,
                                             String description) {
    }

    public record ApiError(String message) {
    }
}
