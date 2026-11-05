package com.opa.permission.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opa.permission.model.AttributeDefinition;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Component
class AttributeValueValidator {
    private final ObjectMapper objectMapper;

    AttributeValueValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    ValidationResult validate(AttributeDefinition definition, String valueText) {
        if (valueText == null) {
            return ValidationResult.invalid("valueText is required");
        }

        String dataType = definition.getDataType() == null
                ? ""
                : definition.getDataType().trim().toLowerCase(Locale.ROOT);
        String normalizedValue = valueText;
        try {
            switch (dataType) {
                case "string" -> {
                    return ValidationResult.valid(valueText);
                }
                case "number" -> {
                    normalizedValue = valueText.trim();
                    new BigDecimal(normalizedValue);
                }
                case "boolean" -> {
                    normalizedValue = valueText.trim().toLowerCase(Locale.ROOT);
                    if (!"true".equals(normalizedValue) && !"false".equals(normalizedValue)) {
                        return ValidationResult.invalid("valueText must be true or false");
                    }
                }
                case "date" -> {
                    normalizedValue = valueText.trim();
                    LocalDate.parse(normalizedValue);
                }
                case "datetime" -> {
                    normalizedValue = valueText.trim();
                    OffsetDateTime.parse(normalizedValue);
                }
                case "json" -> {
                    normalizedValue = valueText.trim();
                    if (objectMapper.readTree(normalizedValue) == null) {
                        return ValidationResult.invalid("valueText must contain valid JSON");
                    }
                }
                default -> {
                    return ValidationResult.invalid("Attribute has an unsupported data type");
                }
            }
        } catch (NumberFormatException | DateTimeParseException exception) {
            return ValidationResult.invalid("valueText does not match the attribute data type");
        } catch (JsonProcessingException exception) {
            return ValidationResult.invalid("valueText must contain valid JSON");
        }
        return ValidationResult.valid(normalizedValue);
    }

    record ValidationResult(String valueText, String error) {
        static ValidationResult valid(String valueText) {
            return new ValidationResult(valueText, null);
        }

        static ValidationResult invalid(String error) {
            return new ValidationResult(null, error);
        }

        boolean isValid() {
            return error == null;
        }
    }
}
