package com.opa.permission.service;

public final class TrinoResourceCode {
    private static final int MAX_CODE_LENGTH = 384;

    private TrinoResourceCode() {
    }

    public static String generate(long tenantId, String resourceType, String resourceName) {
        if (tenantId <= 0 || resourceType == null || resourceType.isBlank()
                || resourceName == null || resourceName.isBlank()) {
            throw new IllegalArgumentException("Tenant ID, resource type, and resource name are required");
        }

        String prefix = "tenant:" + tenantId + ":" + resourceType + ":";
        String resourceCode = prefix + resourceName;
        if (resourceCode.codePointCount(0, resourceCode.length()) > MAX_CODE_LENGTH) {
            throw new IllegalArgumentException("Generated Trino resource code exceeds 384 characters");
        }
        return resourceCode;
    }
}
