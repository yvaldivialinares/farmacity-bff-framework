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
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Step definitions for the Shopping domain: carts, items, coupons, checkout.
 *
 * State propagation through a full checkout flow:
 *   1. createCart()       → stores CART_ID in context
 *   2. addItemToCart()    → reads CART_ID, stores CART_ITEM_ID
 *   3. confirmCheckout()  → reads CART_ID, stores ORDER_ID
 *   4. Order steps then read ORDER_ID for history/detail assertions
 *
 * Cleanup:
 * - Carts that reach checkout become orders and don't need deletion.
 * - Carts that fail mid-flow register an abandonment action (no BFF delete
 *   endpoint exists; this is a placeholder for future cart TTL handling).
 */
public class ShoppingCartSteps {

    private final ApiClient apiClient;
    private final ScenarioContext context;
    private final CleanupRegistry cleanupRegistry;
    private final AuthHeaderProvider authHeaderProvider;

    public ShoppingCartSteps(ApiClient apiClient, ScenarioContext context,
                             CleanupRegistry cleanupRegistry, AuthHeaderProvider authHeaderProvider) {
        this.apiClient = apiClient;
        this.context = context;
        this.cleanupRegistry = cleanupRegistry;
        this.authHeaderProvider = authHeaderProvider;
    }

    // ─── Cart lifecycle ────────────────────────────────────────────────

    @Given("the user has shopping cart creation data")
    public void userHasShoppingCartCreationData() {
        String dni = context.get(CtxKeys.CUSTOMER_DNI, String.class);

        Response customerResponse = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.GET)
                        .path(PathBuilder.of(ApiEndpoints.CUSTOMER).with("dni", dni).build())
                        .headers(vtexHeaders())
                        .build()
        );
        AssertionUtils.assertStatusCode(customerResponse, 200);

        String healthInsuranceId = firstNonBlank(customerResponse,
                "healthInsuranceID",
                "healthInsurance.id",
                "healthInsuranceId.value");
        assertRequiredField(healthInsuranceId, "healthInsuranceId", "/api/v1/customers/{dni}");
        context.set(CtxKeys.HEALTH_INSURANCE_ID, healthInsuranceId);

        Response addressesResponse = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.GET)
                        .path(PathBuilder.of(ApiEndpoints.CUSTOMER_ADDRESSES).with("dni", dni).build())
                        .headers(vtexHeaders())
                        .build()
        );
        AssertionUtils.assertStatusCode(addressesResponse, 200);

        String addressId = firstNonBlank(addressesResponse,
                "[0].id",
                "addresses[0].id",
                "data[0].id");
        assertRequiredField(addressId, "addressId", "/api/v1/customers/{dni}/addresses");
        context.set(CtxKeys.ADDRESS_ID, addressId);

        Response productsResponse = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.GET)
                        .path(ApiEndpoints.SEARCH_PRODUCTS)
                        .headers(vtexHeaders())
                        .queryParams(Map.of("page", 1, "quantity", 1))
                        .build()
        );
        AssertionUtils.assertStatusCode(productsResponse, 200);

        String sku = firstNonBlank(productsResponse,
                "products[0].sku",
                "products[0].variations[0].sku",
                "[0].sku");
        assertRequiredField(sku, "sku", "/api/v1/search/products");
        context.set(CtxKeys.PRODUCT_SKU, sku);

        Map<String, Object> createCartBody = new HashMap<>();
        createCartBody.put("dni", dni);
        createCartBody.put("storeId", 1);
        createCartBody.put("addressId", addressId);
        createCartBody.put("healthInsuranceId", healthInsuranceId);
        createCartBody.put("isDelivery", true);
        createCartBody.put("items", List.of(Map.of("sku", sku, "quantity", 1)));
        context.set(CtxKeys.CART_CREATE_BODY, createCartBody);
    }

    @When("the user creates a new shopping cart")
    public void userCreatesNewCart() {
        Map<String, Object> body = new HashMap<>();
        body.put("dni", context.get(CtxKeys.CUSTOMER_DNI, String.class));

        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.POST)
                        .path(ApiEndpoints.CARTS)
                        .headers(vtexHeaders())
                        .body(body)
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);

        if (response.getStatusCode() == 201 || response.getStatusCode() == 200) {
            String cartId = response.jsonPath().getString("shoppingCartId");
            if (cartId == null || cartId.isBlank()) {
                cartId = response.jsonPath().getString("id");
            }
            context.set(CtxKeys.CART_ID, cartId);
        }
    }

    @When("the user creates a new shopping cart with prepared data")
    @SuppressWarnings("unchecked")
    public void userCreatesNewCartWithPreparedData() {
        Map<String, Object> body = context.get(CtxKeys.CART_CREATE_BODY, Map.class);

        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.POST)
                        .path(ApiEndpoints.CARTS)
                        .headers(vtexHeaders())
                        .body(body)
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);

        if (response.getStatusCode() == 201 || response.getStatusCode() == 200) {
            String cartId = response.jsonPath().getString("shoppingCartId");
            if (cartId == null || cartId.isBlank()) {
                cartId = response.jsonPath().getString("id");
            }
            context.set(CtxKeys.CART_ID, cartId);
        }
    }

    @When("the user retrieves the cart")
    public void userRetrievesCart() {
        String path = PathBuilder.of(ApiEndpoints.CART)
                .with("id", context.get(CtxKeys.CART_ID, String.class))
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

    // ─── Items ─────────────────────────────────────────────────────────

    @When("the user adds product with SKU {string} and quantity {int} to the cart")
    public void userAddsItemToCart(String sku, int quantity) {
        String cartId = context.get(CtxKeys.CART_ID, String.class);
        String path = PathBuilder.of(ApiEndpoints.CART_ITEMS).with("id", cartId).build();

        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.POST)
                        .path(path)
                        .headers(vtexHeaders())
                        .body(Map.of("sku", sku, "quantity", quantity))
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);

        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            String itemId = response.jsonPath().getString("orderItemId");
            context.set(CtxKeys.CART_ITEM_ID, itemId);
        }
    }

    @When("the user updates the cart item quantity to {int}")
    public void userUpdatesCartItemQuantity(int newQuantity) {
        String cartId = context.get(CtxKeys.CART_ID, String.class);
        String itemId = context.get(CtxKeys.CART_ITEM_ID, String.class);
        String path = PathBuilder.of(ApiEndpoints.CART_ITEM)
                .with("id", cartId).with("itemId", itemId).build();

        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.PUT)
                        .path(path)
                        .headers(vtexHeaders())
                        .body(Map.of("quantity", newQuantity))
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);
    }

    @When("the user removes the item from the cart")
    public void userRemovesItemFromCart() {
        String cartId = context.get(CtxKeys.CART_ID, String.class);
        String itemId = context.get(CtxKeys.CART_ITEM_ID, String.class);
        String path = PathBuilder.of(ApiEndpoints.CART_ITEM)
                .with("id", cartId).with("itemId", itemId).build();

        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.DELETE)
                        .path(path)
                        .headers(vtexHeaders())
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);
    }

    // ─── Coupons ───────────────────────────────────────────────────────

    @When("the user applies coupon {string}")
    public void userAppliesCoupon(String couponCode) {
        String cartId = context.get(CtxKeys.CART_ID, String.class);
        String path = PathBuilder.of(ApiEndpoints.CART_COUPONS).with("id", cartId).build();

        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.POST)
                        .path(path)
                        .headers(vtexHeaders())
                        .body(Map.of("couponCode", couponCode))
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);

        // Register coupon removal in case checkout doesn't happen
        if (response.getStatusCode() == 200) {
            cleanupRegistry.register(
                    "Remove coupon [" + couponCode + "] from cart [" + cartId + "]",
                    () -> removeCoupon(cartId)
            );
        }
    }

    // ─── Checkout ──────────────────────────────────────────────────────

    @When("the user confirms checkout")
    public void userConfirmsCheckout() {
        String cartId = context.get(CtxKeys.CART_ID, String.class);
        String path = PathBuilder.of(ApiEndpoints.CART_CHECKOUT).with("id", cartId).build();

        Response response = apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.POST)
                        .path(path)
                        .headers(vtexHeaders())
                        .build()
        );
        context.set(CtxKeys.LAST_RESPONSE, response);

        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            String orderId = response.jsonPath().getString("orderId");
            context.set(CtxKeys.ORDER_ID, orderId);
        }
    }

    // ─── Orders ────────────────────────────────────────────────────────

    @When("the user retrieves their order history")
    public void userRetrievesOrderHistory() {
        String path = PathBuilder.of(ApiEndpoints.ORDERS_BY_CUSTOMER)
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

    @When("the user retrieves the order details")
    public void userRetrievesOrderDetails() {
        String path = PathBuilder.of(ApiEndpoints.ORDER)
                .with("orderId", context.get(CtxKeys.ORDER_ID, String.class))
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

    // ─── Assertions ────────────────────────────────────────────────────

    @Then("the cart is created successfully")
    public void theCartIsCreatedSuccessfully() {
        Response response = context.get(CtxKeys.LAST_RESPONSE, Response.class);
        int statusCode = response.getStatusCode();
        Assertions.assertTrue(
                statusCode == 200 || statusCode == 201,
                "Expected status 200 or 201 but was " + statusCode
        );

        String shoppingCartId = response.jsonPath().getString("shoppingCartId");
        String legacyId = response.jsonPath().getString("id");
        Assertions.assertTrue(
                (shoppingCartId != null && !shoppingCartId.isBlank())
                        || (legacyId != null && !legacyId.isBlank()),
                "Expected non-empty shoppingCartId or id in create cart response"
        );
    }

    @Then("the item is added to the cart")
    public void theItemIsAddedToTheCart() {
        Response response = context.get(CtxKeys.LAST_RESPONSE, Response.class);
        AssertionUtils.assertStatusCode(response, 200);
        AssertionUtils.assertFieldNotNull(response, "orderItemId");
    }

    @Then("the order is created successfully")
    public void theOrderIsCreatedSuccessfully() {
        Response response = context.get(CtxKeys.LAST_RESPONSE, Response.class);
        AssertionUtils.assertStatusCode(response, 201);
        AssertionUtils.assertFieldNotNull(response, "orderId");
    }

    @Then("the order status is {string}")
    public void theOrderStatusIs(String expectedStatus) {
        Response response = context.get(CtxKeys.LAST_RESPONSE, Response.class);
        AssertionUtils.assertFieldEquals(response, "status", expectedStatus);
    }

    @Then("the new order appears in the history")
    public void theNewOrderAppearsInHistory() {
        String orderId = context.get(CtxKeys.ORDER_ID, String.class);
        Response response = context.get(CtxKeys.LAST_RESPONSE, Response.class);
        AssertionUtils.assertListContains(response, "orderId", orderId);
    }

    // ─── Private helpers ───────────────────────────────────────────────

    private void removeCoupon(String cartId) {
        String path = PathBuilder.of(ApiEndpoints.CART_COUPONS).with("id", cartId).build();
        apiClient.execute(
                RequestSpec.builder()
                        .method(HttpMethod.DELETE)
                        .path(path)
                        .headers(vtexHeaders())
                        .build()
        );
    }

    private String firstNonBlank(Response response, String... jsonPaths) {
        List<String> candidates = new ArrayList<>();
        for (String jsonPath : jsonPaths) {
            String value = response.jsonPath().getString(jsonPath);
            if (value != null && !value.isBlank() && !"null".equalsIgnoreCase(value)) {
                return value;
            }
            candidates.add(jsonPath);
        }
        return null;
    }

    private void assertRequiredField(String value, String fieldName, String sourceEndpoint) {
        Assertions.assertTrue(
                value != null && !value.isBlank(),
                "Missing required field '" + fieldName + "' from " + sourceEndpoint
                        + ". Cannot build cart create payload."
        );
    }

    private Map<String, String> vtexHeaders() {
        return authHeaderProvider.getHeaders(
                AuthScheme.VTEX_TOKEN,
                context.get(CtxKeys.VTEX_TOKEN, String.class)
        );
    }
}
