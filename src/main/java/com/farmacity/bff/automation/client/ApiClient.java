package com.farmacity.bff.automation.client;

import com.farmacity.bff.automation.config.ConfigLoader;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;

/**
 * Single generic HTTP execution engine for the entire framework.
 *
 * All 66 BFF endpoints are called through this class.
 * No endpoint knowledge lives here — only the mechanics of executing
 * a RequestSpec and returning a RestAssured Response.
 *
 * PicoContainer creates one instance per scenario, shared across all step classes.
 */
public class ApiClient {

    private static final Logger log = LoggerFactory.getLogger(ApiClient.class);

    public ApiClient() {
        RestAssured.baseURI = ConfigLoader.getInstance().getBaseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    public Response execute(RequestSpec spec) {
        int attempt = 0;
        Response response = null;

        do {
            if (attempt > 0) {
                sleep(spec.getRetryDelayMs() * attempt);
                log.info("Retry [{}/{}] → {} {}", attempt, spec.getMaxRetries(),
                        spec.getMethod(), spec.getPath());
            }
            response = doExecute(spec);
            attempt++;
        } while (!spec.getRetryOnStatusCodes().isEmpty()
                && attempt <= spec.getMaxRetries()
                && spec.getRetryOnStatusCodes().contains(response.getStatusCode()));

        attachToAllure(spec, response);
        return response;
    }

    private Response doExecute(RequestSpec spec) {
        RequestSpecification req = given()
                .headers(spec.getHeaders())
                .queryParams(spec.getQueryParams())
                .log().all();

        if (spec.getBody() != null) {
            req = req.contentType("application/json").body(spec.getBody());
        }

        Response response = switch (spec.getMethod()) {
            case GET    -> req.get(spec.getPath());
            case POST   -> req.post(spec.getPath());
            case PUT    -> req.put(spec.getPath());
            case PATCH  -> req.patch(spec.getPath());
            case DELETE -> req.delete(spec.getPath());
        };

        response.then().log().all();
        return response;
    }

    private boolean shouldRetry(Response response, RequestSpec spec) {
        return spec.getRetryOnStatusCodes().contains(response.getStatusCode());
    }

    private void attachToAllure(RequestSpec spec, Response response) {
        String content = "→ " + spec.getMethod() + " " + spec.getPath()
                + "\n← " + response.getStatusCode()
                + "\n" + response.getBody().asPrettyString();
        Allure.addAttachment(spec.getMethod() + " " + spec.getPath(), "text/plain", content);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
