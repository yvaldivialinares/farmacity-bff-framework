package com.farmacity.bff.automation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * RFC 7807 Problem Details — the standard error format returned by all BFF endpoints.
 *
 * Example:
 * {
 *   "type":     "https://tools.ietf.org/html/rfc7231#section-6.5.4",
 *   "title":    "Resource Not Found",
 *   "status":   404,
 *   "detail":   "Customer with DNI 12345678 was not found.",
 *   "instance": "/api/v1/customers/12345678",
 *   "traceId":  "00-abc123def456-789-00"
 * }
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProblemDetailsResponse {

    @JsonProperty("type")
    private String type;

    @JsonProperty("title")
    private String title;

    @JsonProperty("status")
    private int status;

    @JsonProperty("detail")
    private String detail;

    @JsonProperty("instance")
    private String instance;

    @JsonProperty("traceId")
    private String traceId;
}
