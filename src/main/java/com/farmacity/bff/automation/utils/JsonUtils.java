package com.farmacity.bff.automation.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;

public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {}

    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Serialization failed for: " + obj.getClass().getSimpleName(), e);
        }
    }

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Deserialization failed to type: " + type.getSimpleName(), e);
        }
    }

    public static <T> T fromResponse(Response response, Class<T> type) {
        return fromJson(response.getBody().asString(), type);
    }

    public static String extractString(Response response, String jsonPath) {
        return response.jsonPath().getString(jsonPath);
    }

    public static <T> T extract(Response response, String jsonPath, Class<T> type) {
        return response.jsonPath().getObject(jsonPath, type);
    }

    public static int extractInt(Response response, String jsonPath) {
        return response.jsonPath().getInt(jsonPath);
    }
}
