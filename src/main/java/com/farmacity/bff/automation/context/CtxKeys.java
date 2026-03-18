package com.farmacity.bff.automation.context;

/**
 * All ScenarioContext key constants in one place.
 * Using constants instead of magic strings prevents typos and makes
 * it easy to trace which steps read/write each piece of state.
 */
public final class CtxKeys {

    private CtxKeys() {}

    // ─── Auth ─────────────────────────────────────────────────────────
    public static final String VTEX_TOKEN       = "vtex.token";

    // ─── Customer ─────────────────────────────────────────────────────
    public static final String CUSTOMER_DNI     = "customer.dni";
    public static final String CUSTOMER_EMAIL   = "customer.email";
    public static final String ADDRESS_ID       = "address.id";
    public static final String FAVORITE_SKU     = "favorite.sku";

    // ─── Shopping ─────────────────────────────────────────────────────
    public static final String CART_ID          = "cart.id";
    public static final String CART_ITEM_ID     = "cart.item.id";
    public static final String ORDER_ID         = "order.id";

    // ─── Catalog ──────────────────────────────────────────────────────
    public static final String PRODUCT_SKU      = "product.sku";

    // ─── Stores ───────────────────────────────────────────────────────
    public static final String CHECKIN_ID       = "checkin.id";

    // ─── Prescriptions ────────────────────────────────────────────────
    public static final String PRESCRIPTION_ID  = "prescription.id";

    // ─── Identity Validation ──────────────────────────────────────────
    public static final String VERIFICATION_ID  = "verification.id";

    // ─── HTTP ─────────────────────────────────────────────────────────
    /** Stores the RestAssured Response from the most recent HTTP call. */
    public static final String LAST_RESPONSE    = "last.response";
}
