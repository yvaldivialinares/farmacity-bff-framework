package com.farmacity.bff.automation.steps;

import com.farmacity.bff.automation.auth.AuthHeaderProvider;
import com.farmacity.bff.automation.auth.AuthScheme;
import com.farmacity.bff.automation.client.ApiClient;
import com.farmacity.bff.automation.client.HttpMethod;
import com.farmacity.bff.automation.client.RequestSpec;
import com.farmacity.bff.automation.context.CtxKeys;
import com.farmacity.bff.automation.context.ScenarioContext;
import com.farmacity.bff.automation.utils.AssertionUtils;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.Map;

/**
 * Generic HTTP step definitions that work against any endpoint.
 *
 * These steps are useful during early development (Phase 1-2) when domain-specific
 * step classes don't exist yet, and for quick one-off assertions in flow scenarios.
 *
 * Auth is resolved automatically from ScenarioContext:
 * - If a VTEX_TOKEN is present → Authorization: Bearer vtex header is added
 * - Otherwise → anonymous (no auth header)
 */
public class GenericApiSteps {

    private final ApiClient apiClient;
    private final ScenarioContext context;
    private final AuthHeaderProvider authHeaderProvider;

    public GenericApiSteps(ApiClient apiClient, ScenarioContext context, AuthHeaderProvider authHeaderProvider) {
        this.apiClient = apiClient;
        this.context = context;
        this.authHeaderProvider = authHeaderProvider;
    }

    @When("I send a {word} request to {string}")
    public void iSendRequest(String method, String path) {
        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.valueOf(method.toUpperCase()))
                        .path(path)
                        .headers(resolveAuthHeaders())
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);
    }

    @When("I send a {word} request to {string} with body:")
    public void iSendRequestWithBody(String method, String path, String docString) {
        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.valueOf(method.toUpperCase()))
                        .path(path)
                        .headers(resolveAuthHeaders())
                        .body(docString)
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);
    }

    @When("I send a {word} request to {string} with query param {string} = {string}")
    public void iSendRequestWithQueryParam(String method, String path, String paramName, String paramValue) {
        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.valueOf(method.toUpperCase()))
                        .path(path)
                        .headers(resolveAuthHeaders())
                        .queryParams(Map.of(paramName, paramValue))
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);
    }

    @Then("the response status code is {int}")
    public void theResponseStatusCodeIs(int expectedStatus) {
        Response response = context.get(CtxKeys.LAST_RESPONSE, Response.class);
        AssertionUtils.assertStatusCode(response, expectedStatus);
    }

    @Then("the response field {string} equals {string}")
    public void theResponseFieldEquals(String jsonPath, String expected) {
        Response response = context.get(CtxKeys.LAST_RESPONSE, Response.class);
        AssertionUtils.assertFieldEquals(response, jsonPath, expected);
    }

    @Then("the response field {string} equals {int}")
    public void theResponseFieldEqualsInt(String jsonPath, int expected) {
        Response response = context.get(CtxKeys.LAST_RESPONSE, Response.class);
        AssertionUtils.assertFieldEquals(response, jsonPath, expected);
    }

    @Then("the response field {string} is not null")
    public void theResponseFieldIsNotNull(String jsonPath) {
        Response response = context.get(CtxKeys.LAST_RESPONSE, Response.class);
        AssertionUtils.assertFieldNotNull(response, jsonPath);
    }

    @Then("the response field {string} is not empty")
    public void theResponseFieldIsNotEmpty(String jsonPath) {
        Response response = context.get(CtxKeys.LAST_RESPONSE, Response.class);
        AssertionUtils.assertFieldNotEmpty(response, jsonPath);
    }

    @Then("the response matches the error schema with status {int}")
    public void theResponseMatchesErrorSchema(int expectedStatus) {
        Response response = context.get(CtxKeys.LAST_RESPONSE, Response.class);
        AssertionUtils.assertProblemDetails(response, expectedStatus);
    }

    @Then("the response list {string} is not empty")
    public void theResponseListIsNotEmpty(String jsonPath) {
        Response response = context.get(CtxKeys.LAST_RESPONSE, Response.class);
        AssertionUtils.assertListNotEmpty(response, jsonPath);
    }

    @Then("I store the response field {string} as {string}")
    public void iStoreResponseField(String jsonPath, String contextKey) {
        Response response = context.get(CtxKeys.LAST_RESPONSE, Response.class);
        String value = response.jsonPath().getString(jsonPath);
        context.set(contextKey, value);
    }

    private Map<String, String> resolveAuthHeaders() {
        if (context.has(CtxKeys.VTEX_TOKEN)) {
            return authHeaderProvider.getHeaders(
                    AuthScheme.VTEX_TOKEN,
                    context.get(CtxKeys.VTEX_TOKEN, String.class)
            );
        }
        return authHeaderProvider.getHeaders(AuthScheme.ANONYMOUS, null);
    }
}
