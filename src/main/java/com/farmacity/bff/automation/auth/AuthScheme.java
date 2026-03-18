package com.farmacity.bff.automation.auth;

/**
 * The four authentication schemes supported by the Farmacity BFF.
 *
 * VTEX_TOKEN  → Authorization: Bearer vtex <token>  (most endpoints)
 * SSO_TOKEN   → Authorization: VtexSsoToken <token>  (SSO login flow)
 * API_KEY     → X-Api-Key: <key>                     (/admin/* only)
 * ANONYMOUS   → no header required                   ([AllowAnonymous] endpoints)
 */
public enum AuthScheme {
    VTEX_TOKEN,
    SSO_TOKEN,
    API_KEY,
    ANONYMOUS
}
