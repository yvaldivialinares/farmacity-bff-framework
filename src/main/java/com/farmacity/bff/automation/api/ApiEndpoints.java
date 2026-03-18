package com.farmacity.bff.automation.api;

/**
 * All 66 BFF endpoint path templates, organized by domain.
 *
 * Path parameters are expressed as {paramName} placeholders.
 * Use PathBuilder to resolve them before passing to ApiClient.
 *
 * Versioning: v1 is the stable default. v2 variants are listed explicitly
 * where they exist (shopping carts, product search).
 */
public final class ApiEndpoints {

    private ApiEndpoints() {}

    // ─── Authentication ────────────────────────────────────────────────
    public static final String LOGIN                      = "/api/v1/login";
    public static final String LOGIN_OTP_SEND             = "/api/v1/login/code";
    public static final String LOGIN_OTP_VALIDATE         = "/api/v1/login/code/validate";
    public static final String LOGIN_SSO                  = "/api/v1/login/sso";
    public static final String LOGIN_PASSWORD_RECOVERY    = "/api/v1/login/password-recovery";
    public static final String IDENTITY_VERIFY            = "/api/v1/identity-validation/verification";
    public static final String IDENTITY_STATUS            = "/api/v1/identity-validation/status/{dni}";

    // ─── Customers ─────────────────────────────────────────────────────
    public static final String REGISTRATION_START         = "/api/v1/customers/registration/start";
    public static final String REGISTRATION_COMPLETE      = "/api/v1/customers/registration/complete";
    public static final String CUSTOMER                   = "/api/v1/customers/{dni}";
    public static final String CUSTOMER_PROFILE_COMPLETION = "/api/v1/customers/{dni}/profile-completion";
    public static final String CUSTOMER_ACCESS_CODE       = "/api/v1/customers/access-code";
    public static final String CUSTOMER_ADDRESSES         = "/api/v1/customers/{dni}/addresses";
    public static final String CUSTOMER_ADDRESS           = "/api/v1/customers/{dni}/addresses/{id}";
    public static final String CUSTOMER_ADDRESS_DEFAULT   = "/api/v1/customers/{dni}/addresses/{id}/default";
    public static final String CUSTOMER_FAVORITES         = "/api/v1/customers/{dni}/favorites";
    public static final String CUSTOMER_FAVORITE          = "/api/v1/customers/{dni}/favorites/{sku}";
    public static final String HEALTH_INSURANCES          = "/api/v1/health-insurances";

    // ─── Shopping Carts ────────────────────────────────────────────────
    public static final String CARTS                      = "/api/v1/shopping-carts";
    public static final String CARTS_V2                   = "/api/v2/shopping-carts";
    public static final String CART                       = "/api/v1/shopping-carts/{id}";
    public static final String CART_ITEMS                 = "/api/v1/shopping-carts/{id}/items";
    public static final String CART_ITEM                  = "/api/v1/shopping-carts/{id}/items/{itemId}";
    public static final String CART_COUPONS               = "/api/v1/shopping-carts/{id}/coupons";
    public static final String CART_SHIPPING              = "/api/v1/shopping-carts/{id}/shipping";
    public static final String CART_PAYMENT               = "/api/v1/shopping-carts/{id}/payment";
    public static final String CART_CHECKOUT              = "/api/v1/shopping-carts/{id}/checkout";

    // ─── Orders ────────────────────────────────────────────────────────
    public static final String ORDERS_BY_CUSTOMER         = "/api/v1/orders/customers/{dni}";
    public static final String ORDER                      = "/api/v1/orders/{orderId}";

    // ─── Catalog ───────────────────────────────────────────────────────
    public static final String SEARCH_PRODUCTS            = "/api/v1/search/products";
    public static final String SEARCH_PRODUCTS_V2         = "/api/v2/search/products";
    public static final String CATEGORIES                 = "/api/v1/categories";
    public static final String INVENTORY_BY_SKU           = "/api/v1/inventory/{sku}";
    public static final String INVENTORY_BATCH            = "/api/v1/inventory/batch";
    public static final String INVENTORY_DELTA            = "/api/v1/inventory/delta";
    public static final String INVENTORY_BY_STORE         = "/api/v1/inventory/stores/{storeId}";
    public static final String INVENTORY_RESERVATION      = "/api/v1/inventory/reservation";
    public static final String PRODUCT_REVIEWS            = "/api/v1/reviews/{productId}";
    public static final String CREATE_REVIEW              = "/api/v1/reviews";

    // ─── Stores & Check-ins ────────────────────────────────────────────
    public static final String STORES                     = "/api/v1/stores";
    public static final String CHECKINS                   = "/api/v1/checkins";
    public static final String CHECKIN                    = "/api/v1/checkins/{checkinId}";

    // ─── Prescriptions ─────────────────────────────────────────────────
    public static final String PRESCRIPTIONS              = "/api/v1/prescriptions/{dni}";
    public static final String PRESCRIPTION_REDEEM        = "/api/v1/prescriptions/{prescriptionId}/redeem";
    public static final String PRESCRIPTION_DETAILS       = "/api/v1/prescriptions/{prescriptionId}/details";

    // ─── System & Admin ────────────────────────────────────────────────
    public static final String CONFIG_SHIPPING            = "/api/v1/configurations/shipping-costs";
    public static final String CONFIG_STYLES              = "/api/v1/configurations/app-styles";
    public static final String RETAIL_MEDIA_CAMPAIGNS     = "/api/v1/retail-media/campaigns";
    public static final String ADMIN_CACHE_REFRESH        = "/api/v1/admin/cache/refresh";
    public static final String ADMIN_CACHE_CLEAR          = "/api/v1/admin/cache/clear";
    public static final String ADMIN_HEALTH               = "/api/v1/admin/health";
    public static final String ADMIN_METRICS              = "/api/v1/admin/metrics";
}
