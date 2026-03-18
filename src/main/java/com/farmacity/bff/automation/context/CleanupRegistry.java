package com.farmacity.bff.automation.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * LIFO stack of cleanup actions to run after each scenario.
 *
 * Any step that creates a resource (address, favorite, cart item, etc.) must
 * immediately register a corresponding delete action here. CucumberHooks @After
 * calls executeAll(), which pops and runs each action in reverse registration order.
 *
 * This ensures scenarios always start from a clean slate regardless of pass/fail.
 *
 * Registration example (inside a step method):
 * <pre>
 *   cleanupRegistry.register(
 *       "Delete address [" + addressId + "]",
 *       () -> apiClient.execute(deleteAddressSpec(dni, addressId))
 *   );
 * </pre>
 *
 * Key properties:
 * - LIFO order: if you create A then B, B is deleted first (safe for dependent resources)
 * - Failures are swallowed with a warning so one failed cleanup doesn't block others
 * - PicoContainer creates one instance per scenario
 */
public class CleanupRegistry {

    private static final Logger log = LoggerFactory.getLogger(CleanupRegistry.class);

    private final Deque<CleanupAction> actions = new ArrayDeque<>();

    public void register(String description, Runnable action) {
        actions.push(new CleanupAction(description, action));
        log.debug("Cleanup registered: {}", description);
    }

    public void executeAll() {
        if (actions.isEmpty()) return;

        log.info("Running {} cleanup action(s)...", actions.size());
        while (!actions.isEmpty()) {
            CleanupAction action = actions.pop();
            try {
                log.info("  ⟳ {}", action.description());
                action.runnable().run();
            } catch (Exception e) {
                log.warn("  ✗ Cleanup failed [{}]: {}", action.description(), e.getMessage());
            }
        }
    }

    public boolean isEmpty() {
        return actions.isEmpty();
    }

    private record CleanupAction(String description, Runnable runnable) {}
}
