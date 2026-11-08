package com.opa.permission.service;

public class OpaDecisionException extends RuntimeException {
    public OpaDecisionException(String message) {
        super(message);
    }

    public OpaDecisionException(String message, Throwable cause) {
        super(message, cause);
    }
}
