package com.farmacity.bff.automation.auth;

import com.farmacity.bff.automation.config.ConfigLoader;

import java.util.Collections;
import java.util.Map;

/**
 * Translates an AuthScheme + token value into the correct HTTP header map
 * expected by the BFF authentication middleware.
 *
 * PicoContainer creates one instance per scenario, shared across step classes.
 */
public class AuthHeaderProvider {

    public Map<String, String> getHeaders(AuthScheme scheme, String token) {
        return switch (scheme) {
            case VTEX_TOKEN -> Map.of("Authorization", "Bearer vtex " + token);
            case SSO_TOKEN  -> Map.of("Authorization", "VtexSsoToken " + token);
            case API_KEY    -> Map.of("X-Api-Key", ConfigLoader.getInstance().getAdminApiKey());
            case ANONYMOUS  -> Collections.emptyMap();
        };
    }
}
