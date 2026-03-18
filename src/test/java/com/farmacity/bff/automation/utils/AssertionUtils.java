package com.farmacity.bff.automation.utils;

import io.restassured.response.Response;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Domain-aware assertion helpers built on top of JUnit 5.
 *
 * All methods produce descriptive failure messages that include the actual
 * response body, making CI failures self-diagnosable without needing to
 * re-run the test locally.
 */
public final class AssertionUtils {

    private AssertionUtils() {}

    public static void assertStatusCode(Response response, int expected) {
        assertEquals(expected, response.getStatusCode(),
                () -> "Expected HTTP " + expected + " but got " + response.getStatusCode()
                        + "\nBody:\n" + response.getBody().asPrettyString());
    }

    public static void assertFieldEquals(Response response, String jsonPath, Object expected) {
        Object actual = response.jsonPath().get(jsonPath);
        assertEquals(expected, actual,
                () -> "Field [" + jsonPath + "] → expected [" + expected + "] but was [" + actual + "]"
                        + "\nBody:\n" + response.getBody().asPrettyString());
    }

    public static void assertFieldNotNull(Response response, String jsonPath) {
        Object value = response.jsonPath().get(jsonPath);
        assertNotNull(value,
                () -> "Field [" + jsonPath + "] should not be null"
                        + "\nBody:\n" + response.getBody().asPrettyString());
    }

    public static void assertFieldNotEmpty(Response response, String jsonPath) {
        String value = response.jsonPath().getString(jsonPath);
        assertNotNull(value, () -> "Field [" + jsonPath + "] is null");
        assertFalse(value.isBlank(),
                () -> "Field [" + jsonPath + "] should not be blank"
                        + "\nBody:\n" + response.getBody().asPrettyString());
    }

    /**
     * Validates the RFC 7807 Problem Details error structure returned by all BFF error responses.
     * Checks: status code, status field in body, non-empty title, non-null traceId.
     */
    public static void assertProblemDetails(Response response, int expectedStatus) {
        assertStatusCode(response, expectedStatus);
        assertFieldEquals(response, "status", expectedStatus);
        assertFieldNotEmpty(response, "title");
        assertFieldNotNull(response, "traceId");
    }

    public static void assertListNotEmpty(Response response, String jsonPath) {
        List<?> list = response.jsonPath().getList(jsonPath);
        assertNotNull(list, () -> "List at [" + jsonPath + "] is null");
        assertFalse(list.isEmpty(),
                () -> "List at [" + jsonPath + "] should not be empty"
                        + "\nBody:\n" + response.getBody().asPrettyString());
    }

    public static void assertListContains(Response response, String jsonPath, String expectedValue) {
        List<String> list = response.jsonPath().getList(jsonPath, String.class);
        assertNotNull(list, () -> "List at [" + jsonPath + "] is null");
        assertTrue(list.contains(expectedValue),
                () -> "List at [" + jsonPath + "] does not contain [" + expectedValue + "]. "
                        + "Actual: " + list);
    }

    public static void assertIntGreaterThan(Response response, String jsonPath, int threshold) {
        int value = response.jsonPath().getInt(jsonPath);
        assertTrue(value > threshold,
                () -> "Field [" + jsonPath + "] should be > " + threshold + " but was " + value);
    }
}
