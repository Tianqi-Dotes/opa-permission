package com.opa.permission.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.opa.permission.model.AttributeDefinition;
import com.opa.permission.model.SubjectAttributeValue;
import com.opa.permission.model.UserAccount;
import com.opa.permission.service.AttributeDefinitionService;
import com.opa.permission.service.SubjectAttributeValueService;
import com.opa.permission.service.UserAccountService;
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
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/abac/subject-attributes")
public class SubjectAttributeValueController {
    private final SubjectAttributeValueService subjectAttributeValueService;
    private final AttributeDefinitionService attributeDefinitionService;
    private final UserAccountService userAccountService;
    private final AttributeValueValidator valueValidator;

    public SubjectAttributeValueController(SubjectAttributeValueService subjectAttributeValueService,
                                           AttributeDefinitionService attributeDefinitionService,
                                           UserAccountService userAccountService,
                                           AttributeValueValidator valueValidator) {
        this.subjectAttributeValueService = subjectAttributeValueService;
        this.attributeDefinitionService = attributeDefinitionService;
        this.userAccountService = userAccountService;
        this.valueValidator = valueValidator;
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "attributeId", required = false) Long attributeId) {
        QueryWrapper<SubjectAttributeValue> query = new QueryWrapper<>();
        if (subjectId != null) {
            query.eq("subject_id", subjectId);
        }
        if (attributeId != null) {
            query.eq("attribute_id", attributeId);
        }
        query.orderByAsc("subject_id", "attribute_id", "id");
        return ResponseEntity.ok(subjectAttributeValueService.list(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Long id) {
        SubjectAttributeValue value = subjectAttributeValueService.getById(id);
        return value == null ? notFound() : ResponseEntity.ok(value);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody(required = false) SubjectAttributeValueRequest request) {
        String requestError = validateRequest(request);
        if (requestError != null) {
            return badRequest(requestError);
        }

        UserAccount subject = userAccountService.getById(request.subjectId());
        if (subject == null) {
            return notFound("Subject not found");
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
        if (hasSingleValueConflict(request.subjectId(), request.attributeId(), null, definition)) {
            return conflict("A value already exists for this single-valued subject attribute");
        }

        long now = Instant.now().toEpochMilli();
        SubjectAttributeValue value = new SubjectAttributeValue();
        value.setSubjectId(request.subjectId());
        value.setAttributeId(request.attributeId());
        value.setValueText(valueResult.valueText());
        value.setCreateTime(now);
        value.setUpdateTime(now);
        if (!subjectAttributeValueService.save(value)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("Subject attribute value was not created"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(value);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id,
                                    @RequestBody(required = false) SubjectAttributeValueRequest request) {
        String requestError = validateRequest(request);
        if (requestError != null) {
            return badRequest(requestError);
        }

        SubjectAttributeValue existing = subjectAttributeValueService.getById(id);
        if (existing == null) {
            return notFound();
        }
        if (userAccountService.getById(request.subjectId()) == null) {
            return notFound("Subject not found");
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
        if (hasSingleValueConflict(request.subjectId(), request.attributeId(), id, definition)) {
            return conflict("A value already exists for this single-valued subject attribute");
        }

        existing.setSubjectId(request.subjectId());
        existing.setAttributeId(request.attributeId());
        existing.setValueText(valueResult.valueText());
        existing.setUpdateTime(Instant.now().toEpochMilli());
        if (!subjectAttributeValueService.updateById(existing)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("Subject attribute value was not updated"));
        }
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        if (subjectAttributeValueService.getById(id) == null) {
            return notFound();
        }
        if (!subjectAttributeValueService.removeById(id)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("Subject attribute value was not deleted"));
        }
        return ResponseEntity.noContent().build();
    }

    private String validateRequest(SubjectAttributeValueRequest request) {
        if (request == null) {
            return "Request body is required";
        }
        if (request.subjectId() == null || request.subjectId() <= 0) {
            return "subjectId must be a positive integer";
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
        if (!"subject".equals(normalize(definition.getCategory()))) {
            return badRequest("attributeId must reference a subject attribute");
        }
        return null;
    }

    private boolean hasSingleValueConflict(Long subjectId, Long attributeId, Long excludedId,
                                           AttributeDefinition definition) {
        if (Boolean.TRUE.equals(definition.getMultiValued())) {
            return false;
        }
        QueryWrapper<SubjectAttributeValue> query = new QueryWrapper<>();
        query.eq("subject_id", subjectId).eq("attribute_id", attributeId);
        if (excludedId != null) {
            query.ne("id", excludedId);
        }
        return subjectAttributeValueService.count(query) > 0;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static ResponseEntity<ApiError> badRequest(String message) {
        return ResponseEntity.badRequest().body(new ApiError(message));
    }

    private static ResponseEntity<ApiError> notFound() {
        return notFound("Subject attribute value not found");
    }

    private static ResponseEntity<ApiError> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(message));
    }

    private static ResponseEntity<ApiError> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(message));
    }

    public record SubjectAttributeValueRequest(Long subjectId, Long attributeId, String valueText) {
    }

    public record ApiError(String message) {
    }
}
