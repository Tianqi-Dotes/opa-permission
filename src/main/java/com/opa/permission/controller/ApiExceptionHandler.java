package com.opa.permission.controller;

import com.opa.permission.service.TrinoQueryException;
import com.opa.permission.service.TrinoQueryValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(TrinoQueryValidationException.class)
    public ResponseEntity<ApiError> handleBadRequest(TrinoQueryValidationException exception) {
        return ResponseEntity.badRequest().body(new ApiError(exception.getMessage()));
    }

    @ExceptionHandler(TrinoQueryException.class)
    public ResponseEntity<ApiError> handleTrinoQueryFailure() {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError("Trino query execution failed"));
    }

    public record ApiError(String message) {
    }
}
