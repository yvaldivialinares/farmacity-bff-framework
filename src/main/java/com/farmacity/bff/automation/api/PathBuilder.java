package com.farmacity.bff.automation.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves {paramName} placeholders in endpoint path templates.
 *
 * Usage:
 * <pre>
 *   String path = PathBuilder.of(ApiEndpoints.CUSTOMER_ADDRESS)
 *       .with("dni", "30000001")
 *       .with("id",  "addr-abc-123")
 *       .build();
 *   // → "/api/v1/customers/30000001/addresses/addr-abc-123"
 * </pre>
 *
 * build() throws IllegalStateException if any placeholder remains unresolved,
 * which prevents silent 404s caused by literal "{param}" strings in URLs.
 */
public final class PathBuilder {

    private final String template;
    private final Map<String, String> params = new HashMap<>();

    private PathBuilder(String template) {
        this.template = template;
    }

    public static PathBuilder of(String template) {
        return new PathBuilder(template);
    }

    public PathBuilder with(String param, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Path param [" + param + "] cannot be null or blank in template: " + template
            );
        }
        params.put(param, value);
        return this;
    }

    public String build() {
        String result = template;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        if (result.contains("{")) {
            throw new IllegalStateException(
                    "Unresolved path parameters in [" + result + "]. "
                    + "Provided params: " + params.keySet()
            );
        }
        return result;
    }
}
