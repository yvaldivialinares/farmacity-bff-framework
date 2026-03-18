package com.farmacity.bff.automation.client;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Immutable descriptor for a single HTTP request.
 * Built by step classes and consumed exclusively by ApiClient.
 * Nothing outside ApiClient executes HTTP calls.
 */
@Getter
@Builder
public class RequestSpec {

    private final HttpMethod method;
    private final String path;

    @Builder.Default
    private final Map<String, String> headers = Collections.emptyMap();

    @Builder.Default
    private final Map<String, Object> queryParams = Collections.emptyMap();

    /** Jackson-serializable POJO or raw String. Null for GET/DELETE. */
    private final Object body;

    @Builder.Default
    private final int maxRetries = 0;

    @Builder.Default
    private final long retryDelayMs = 1000L;

    /** HTTP status codes that trigger a retry (e.g. 503, 408). */
    @Builder.Default
    private final List<Integer> retryOnStatusCodes = List.of();
}
