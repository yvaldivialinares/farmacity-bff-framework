package com.farmacity.bff.automation.steps;

import com.farmacity.bff.automation.api.ApiEndpoints;
import com.farmacity.bff.automation.api.PathBuilder;
import com.farmacity.bff.automation.auth.AuthHeaderProvider;
import com.farmacity.bff.automation.auth.AuthScheme;
import com.farmacity.bff.automation.client.ApiClient;
import com.farmacity.bff.automation.client.HttpMethod;
import com.farmacity.bff.automation.client.RequestSpec;
import com.farmacity.bff.automation.context.CleanupRegistry;
import com.farmacity.bff.automation.context.CtxKeys;
import com.farmacity.bff.automation.context.ScenarioContext;
import com.farmacity.bff.automation.utils.AssertionUtils;
import com.farmacity.bff.automation.utils.DataFaker;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Map;

/**
 * Step definitions for the Customers domain: profile, addresses, favorites.
 *
 * CLEANUP PATTERN:
 * Every step that creates a resource (address, favorite) immediately registers
 * a corresponding delete action in CleanupRegistry. CucumberHooks @After will
 * execute all registered cleanups in reverse order (LIFO), regardless of whether
 * the scenario passed or failed. This prevents test pollution across runs.
 */
public class CustomerSteps {

    private final ApiClient apiClient;
    private final ScenarioContext context;
    private final CleanupRegistry cleanupRegistry;
    private final AuthHeaderProvider authHeaderProvider;

    public CustomerSteps(ApiClient apiClient, ScenarioContext context,
                         CleanupRegistry cleanupRegistry, AuthHeaderProvider authHeaderProvider) {
        this.apiClient = apiClient;
        this.context = context;
        this.cleanupRegistry = cleanupRegistry;
        this.authHeaderProvider = authHeaderProvider;
    }

    // ─── Pre-conditions ────────────────────────────────────────────────

    @Given("the customer profile exists for the authenticated user")
    public void theCustomerProfileExists() {
        String path = PathBuilder.of(ApiEndpoints.CUSTOMER)
                .with("dni", context.get(CtxKeys.CUSTOMER_DNI, String.class))
                .build();
        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.GET)
                        .path(path)
                        .headers(vtexHeaders())
                        .build()
        );
        AssertionUtils.assertStatusCode(response, 200);
    }

    // ─── Profile ───────────────────────────────────────────────────────

    @When("the user retrieves their profile")
    public void userRetrievesProfile() {
        String path = PathBuilder.of(ApiEndpoints.CUSTOMER)
                .with("dni", context.get(CtxKeys.CUSTOMER_DNI, String.class))
                .build();
        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.GET)
                        .path(path)
                        .headers(vtexHeaders())
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);
    }

    @When("the user retrieves profile completion")
    public void userRetrievesProfileCompletion() {
        String path = PathBuilder.of(ApiEndpoints.CUSTOMER_PROFILE_COMPLETION)
                .with("dni", context.get(CtxKeys.CUSTOMER_DNI, String.class))
                .build();
        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.GET)
                        .path(path)
                        .headers(vtexHeaders())
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);
    }

    // ─── Addresses ─────────────────────────────────────────────────────

    @When("the user retrieves their addresses")
    public void userRetrievesAddresses() {
        String path = PathBuilder.of(ApiEndpoints.CUSTOMER_ADDRESSES)
                .with("dni", context.get(CtxKeys.CUSTOMER_DNI, String.class))
                .build();
        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.GET)
                        .path(path)
                        .headers(vtexHeaders())
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);
    }

    @When("the user adds a new address")
    public void userAddsNewAddress() {
        String dni = context.get(CtxKeys.CUSTOMER_DNI, String.class);
        String path = PathBuilder.of(ApiEndpoints.CUSTOMER_ADDRESSES).with("dni", dni).build();

        Map<String, Object> body = Map.of(
                "street",    DataFaker.streetAddress(),
                "city",      DataFaker.city(),
                "zipCode",   DataFaker.zipCode(),
                "isDefault", false
        );

        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.POST)
                        .path(path)
                        .headers(vtexHeaders())
                        .body(body)
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);

        if (response.getStatusCode() == 201 || response.getStatusCode() == 200) {
            String addressId = response.jsonPath().getString("id");
            context.set(CtxKeys.ADDRESS_ID, addressId);

            // Register cleanup immediately — runs in @After regardless of scenario outcome
            cleanupRegistry.register(
                    "Delete address [" + addressId + "] for customer [" + dni + "]",
                    () -> deleteAddress(dni, addressId)
            );
        }
    }

    @When("the user deletes the created address")
    public void userDeletesCreatedAddress() {
        String dni       = context.get(CtxKeys.CUSTOMER_DNI, String.class);
        String addressId = context.get(CtxKeys.ADDRESS_ID, String.class);
        Response response = deleteAddress(dni, addressId);
        context.set(CtxKeys.LAST_RESPONSE, response);
    }

    @When("the user sets the created address as default")
    public void userSetsAddressAsDefault() {
        String dni       = context.get(CtxKeys.CUSTOMER_DNI, String.class);
        String addressId = context.get(CtxKeys.ADDRESS_ID, String.class);
        String path = PathBuilder.of(ApiEndpoints.CUSTOMER_ADDRESS_DEFAULT)
                .with("dni", dni).with("id", addressId).build();
        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.PUT)
                        .path(path)
                        .headers(vtexHeaders())
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);
    }

    // ─── Favorites ─────────────────────────────────────────────────────

    @When("the user retrieves their favorites")
    public void userRetrievesFavorites() {
        String path = PathBuilder.of(ApiEndpoints.CUSTOMER_FAVORITES)
                .with("dni", context.get(CtxKeys.CUSTOMER_DNI, String.class))
                .build();
        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.GET)
                        .path(path)
                        .headers(vtexHeaders())
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);
    }

    @When("the user adds product {string} to favorites")
    public void userAddsProductToFavorites(String sku) {
        String dni  = context.get(CtxKeys.CUSTOMER_DNI, String.class);
        String path = PathBuilder.of(ApiEndpoints.CUSTOMER_FAVORITES).with("dni", dni).build();

        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.POST)
                        .path(path)
                        .headers(vtexHeaders())
                        .body(Map.of("sku", sku))
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);
        context.set(CtxKeys.FAVORITE_SKU, sku);

        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            cleanupRegistry.register(
                    "Remove SKU [" + sku + "] from favorites for customer [" + dni + "]",
                    () -> removeFavorite(dni, sku)
            );
        }
    }

    @When("the user removes product {string} from favorites")
    public void userRemovesProductFromFavorites(String sku) {
        String dni = context.get(CtxKeys.CUSTOMER_DNI, String.class);
        Response response = removeFavorite(dni, sku);
        context.set(CtxKeys.LAST_RESPONSE, response);
    }

    // ─── Assertions ────────────────────────────────────────────────────

    @Then("the address is created successfully")
    public void theAddressIsCreatedSuccessfully() {
        Response response = context.get(CtxKeys.LAST_RESPONSE, Response.class);
        AssertionUtils.assertStatusCode(response, 201);
        AssertionUtils.assertFieldNotNull(response, "id");
    }

    @Then("the address list contains the created address")
    public void theAddressListContainsTheCreatedAddress() {
        String addressId = context.get(CtxKeys.ADDRESS_ID, String.class);
        Response response = context.get(CtxKeys.LAST_RESPONSE, Response.class);
        List<String> ids = response.jsonPath().getList("id", String.class);
        Assertions.assertTrue(ids.contains(addressId),
                "Expected address [" + addressId + "] in list but found: " + ids);
    }

    @Then("the profile completion percentage is greater than {int}")
    public void theProfileCompletionIsGreaterThan(int threshold) {
        Response response = context.get(CtxKeys.LAST_RESPONSE, Response.class);
        AssertionUtils.assertIntGreaterThan(response, "percentage", threshold);
    }

    // ─── Private helpers ───────────────────────────────────────────────

    private Response deleteAddress(String dni, String addressId) {
        String path = PathBuilder.of(ApiEndpoints.CUSTOMER_ADDRESS)
                .with("dni", dni).with("id", addressId).build();
        return apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.DELETE)
                        .path(path)
                        .headers(vtexHeaders())
                        .build()
        );
    }

    private Response removeFavorite(String dni, String sku) {
        String path = PathBuilder.of(ApiEndpoints.CUSTOMER_FAVORITE)
                .with("dni", dni).with("sku", sku).build();
        return apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.DELETE)
                        .path(path)
                        .headers(vtexHeaders())
                        .build()
        );
    }

    private Map<String, String> vtexHeaders() {
        return authHeaderProvider.getHeaders(
                AuthScheme.VTEX_TOKEN,
                context.get(CtxKeys.VTEX_TOKEN, String.class)
        );
    }
}
