package com.feedback.notification.config;

/** Environment variables set by Terraform; the only place that reads them. */
public record EnvironmentConfig(String userPoolId, String adminGroup, String sesSender) {

    public static EnvironmentConfig fromEnvironment() {
        return new EnvironmentConfig(required("USER_POOL_ID"), required("ADMIN_GROUP"), required("SES_SENDER"));
    }

    // Fails at init instead of on the first event when the function is misconfigured
    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing environment variable " + name);
        }
        return value;
    }
}
