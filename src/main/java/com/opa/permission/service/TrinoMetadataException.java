package com.opa.permission.service;

public class TrinoMetadataException extends RuntimeException {
    public TrinoMetadataException(String message, Throwable cause) {
        super(message, cause);
    }

    public TrinoMetadataException(String message) {
        super(message);
    }
}
