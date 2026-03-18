package com.farmacity.bff.automation.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe per-scenario key-value store.
 *
 * PicoContainer creates one instance per scenario and injects it into every
 * step class and hook that declares it in its constructor.
 * All state that needs to flow between steps (token, cart ID, order ID, etc.)
 * lives here. Keys are defined as constants in {@link CtxKeys}.
 */
public class ScenarioContext {

    private final Map<String, Object> data = new ConcurrentHashMap<>();

    public void set(String key, Object value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) {
            throw new IllegalStateException(
                    "ScenarioContext missing key: '" + key + "'. "
                    + "Did a previous step forget to store it? Available keys: " + data.keySet()
            );
        }
        return type.cast(value);
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }

    /** Called by CucumberHooks @After to prevent state from bleeding between scenarios. */
    public void clear() {
        data.clear();
    }
}
