package com.farmacity.bff.automation.auth;

import com.farmacity.bff.automation.api.ApiEndpoints;
import com.farmacity.bff.automation.client.ApiClient;
import com.farmacity.bff.automation.client.HttpMethod;
import com.farmacity.bff.automation.client.RequestSpec;
import com.farmacity.bff.automation.config.ConfigLoader;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Manages VTEX token acquisition and caching within a scenario.
 *
 * - One login call is made per scenario (lazy, on first need).
 * - CucumberHooks calls invalidate() in @After so the next scenario starts fresh.
 * - Any step can call login(email, password) to override the default QA credentials
 *   (e.g. for negative auth tests or multi-user flow scenarios).
 */
public class TokenManager {

    private static final Logger log = LoggerFactory.getLogger(TokenManager.class);

    private final ApiClient apiClient;
    private String cachedToken;

    public TokenManager(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /** Returns the cached token, acquiring it from QA config credentials if not yet obtained. */
    public String acquireToken() {
        if (cachedToken == null) {
            cachedToken = login(
                    ConfigLoader.getInstance().getTestUserEmail(),
                    ConfigLoader.getInstance().getTestUserPassword()
            );
        }
        return cachedToken;
    }

    /**
     * Authenticates with explicit credentials and caches the result.
     * Calling this replaces any previously cached token for the current scenario.
     */
    public String login(String email, String password) {
        log.info("Acquiring VTEX token for: {}", email);

        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.POST)
                        .path(ApiEndpoints.LOGIN)
                        .body(Map.of("email", email, "password", password))
                        .build()
        );

        if (response.getStatusCode() != 200) {
            throw new RuntimeException(
                    "Login failed [" + response.getStatusCode() + "]: " + response.getBody().asString()
            );
        }

        cachedToken = response.jsonPath().getString("token");
        log.info("Token acquired successfully for: {}", email);
        return cachedToken;
    }

    /** Called by CucumberHooks @After to ensure no token bleeds between scenarios. */
    public void invalidate() {
        cachedToken = null;
    }
}
