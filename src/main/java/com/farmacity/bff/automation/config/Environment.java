package com.farmacity.bff.automation.config;

public enum Environment {

    DEV("https://one-vision-bff-desa.farmacity.net/"),
    QA("https://one-vision-bff-test.farmacity.net/"),
    UAT("https://one-vision-bff-uat.farmacity.net/"),
    PROD("https://bff.farmacity.com.ar");

    private final String baseUrl;

    Environment(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public static Environment fromString(String value) {
        return switch (value.toLowerCase()) {
            case "dev"  -> DEV;
            case "qa"   -> QA;
            case "uat"  -> UAT;
            case "prod" -> PROD;
            default     -> throw new IllegalArgumentException("Unknown environment: " + value);
        };
    }
}
