package com.farmacity.bff.automation.steps;

import com.farmacity.bff.automation.api.ApiEndpoints;
import com.farmacity.bff.automation.auth.TokenManager;
import com.farmacity.bff.automation.client.ApiClient;
import com.farmacity.bff.automation.client.HttpMethod;
import com.farmacity.bff.automation.client.RequestSpec;
import com.farmacity.bff.automation.config.ConfigLoader;
import com.farmacity.bff.automation.context.CtxKeys;
import com.farmacity.bff.automation.context.ScenarioContext;
import com.farmacity.bff.automation.models.request.LoginRequest;
import com.farmacity.bff.automation.utils.AssertionUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.Map;

public class AuthSteps {

    private final ApiClient apiClient;
    private final ScenarioContext context;
    private final TokenManager tokenManager;

    public AuthSteps(ApiClient apiClient, ScenarioContext context, TokenManager tokenManager) {
        this.apiClient = apiClient;
        this.context = context;
        this.tokenManager = tokenManager;
    }

    /**
     * Pre-condition step: authenticates using the QA environment's shared test account
     * and stores the token + DNI in context for all subsequent steps in the scenario.
     */
    @Given("an authenticated user with standard QA credentials")
    public void anAuthenticatedUserWithStandardQaCredentials() {
        String token = tokenManager.login(
                ConfigLoader.getInstance().getTestUserEmail(),
                ConfigLoader.getInstance().getTestUserPassword()
        );
        context.set(CtxKeys.VTEX_TOKEN, token);
        context.set(CtxKeys.CUSTOMER_DNI, ConfigLoader.getInstance().getTestUserDni());
        context.set(CtxKeys.CUSTOMER_EMAIL, ConfigLoader.getInstance().getTestUserEmail());
    }

    /**
     * Pre-condition step: authenticates with explicit credentials.
     * Useful for negative auth tests or multi-user flow scenarios.
     */
    @Given("an authenticated user with email {string} and password {string}")
    public void anAuthenticatedUserWith(String email, String password) {
        String token = tokenManager.login(email, password);
        context.set(CtxKeys.VTEX_TOKEN, token);
    }

    // ─── Login with email/password ──────────────────────────────────────

    @When("the user logs in with email {string} and password {string}")
    public void userLogsIn(String email, String password) {
        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.POST)
                        .path(ApiEndpoints.LOGIN)
                        .body(new LoginRequest(email, password))
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);
    }

    // ─── OTP flow ──────────────────────────────────────────────────────

    @When("the user requests an OTP for email {string}")
    public void userRequestsOtp(String email) {
        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.POST)
                        .path(ApiEndpoints.LOGIN_OTP_SEND)
                        .body(Map.of("email", email))
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);
    }

    @When("the user validates OTP code {string} for email {string}")
    public void userValidatesOtp(String code, String email) {
        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.POST)
                        .path(ApiEndpoints.LOGIN_OTP_VALIDATE)
                        .body(Map.of("email", email, "code", code))
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);
    }

    // ─── SSO ───────────────────────────────────────────────────────────

    @When("the user logs in with SSO token {string}")
    public void userLogsInWithSso(String ssoToken) {
        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.POST)
                        .path(ApiEndpoints.LOGIN_SSO)
                        .body(Map.of("ssoToken", ssoToken))
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);
    }

    // ─── Password recovery ─────────────────────────────────────────────

    @When("the user initiates password recovery for email {string}")
    public void userInitiatesPasswordRecovery(String email) {
        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.POST)
                        .path(ApiEndpoints.LOGIN_PASSWORD_RECOVERY)
                        .body(Map.of("email", email))
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);
    }

    // ─── Assertions ────────────────────────────────────────────────────

    @Then("the login response contains a valid token")
    public void theLoginResponseContainsAValidToken() {
        Response response = context.get(CtxKeys.LAST_RESPONSE, Response.class);
        AssertionUtils.assertStatusCode(response, 200);
        AssertionUtils.assertFieldNotEmpty(response, "token");
        AssertionUtils.assertFieldNotNull(response, "dni");
        AssertionUtils.assertFieldNotNull(response, "hasTUF");
    }

    /**
     * Stores the token from the last login response into ScenarioContext
     * so that subsequent steps can use it for authenticated calls.
     */
    @Then("the token is stored for subsequent requests")
    public void theTokenIsStoredForSubsequentRequests() {
        Response response = context.get(CtxKeys.LAST_RESPONSE, Response.class);
        String token = response.jsonPath().getString("token");
        String dni   = response.jsonPath().getString("dni");
        context.set(CtxKeys.VTEX_TOKEN, token);
        context.set(CtxKeys.CUSTOMER_DNI, dni);
    }
}
