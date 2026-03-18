package com.farmacity.bff.automation.hooks;

import com.farmacity.bff.automation.auth.TokenManager;
import com.farmacity.bff.automation.context.CleanupRegistry;
import com.farmacity.bff.automation.context.ScenarioContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cross-cutting lifecycle hooks for every scenario.
 *
 * @Before  → logs scenario start
 * @After   → runs CleanupRegistry (LIFO resource deletion), invalidates token, clears context
 *
 * Execution order:
 * 1. @Before runs first — scenario state is clean at this point
 * 2. Scenario steps run — each step may register cleanups and populate context
 * 3. @After runs last  — cleanup happens BEFORE context is cleared so cleanup
 *    lambdas can still read context values they captured at registration time
 *
 * All cleanup failures are swallowed (logged as WARN) to prevent a failed
 * teardown from masking the actual scenario failure.
 */
public class CucumberHooks {

    private static final Logger log = LoggerFactory.getLogger(CucumberHooks.class);

    private final ScenarioContext context;
    private final CleanupRegistry cleanupRegistry;
    private final TokenManager tokenManager;

    public CucumberHooks(ScenarioContext context, CleanupRegistry cleanupRegistry, TokenManager tokenManager) {
        this.context = context;
        this.cleanupRegistry = cleanupRegistry;
        this.tokenManager = tokenManager;
    }

    @Before
    public void setUp(Scenario scenario) {
        log.info("▶ [{}] {}", scenario.getId(), scenario.getName());
        Allure.getLifecycle().updateTestCase(tc -> tc.setName(scenario.getName()));
    }

    @After
    public void tearDown(Scenario scenario) {
        // 1. Run all registered cleanup actions in reverse (LIFO) order
        cleanupRegistry.executeAll();

        // 2. Invalidate cached token so next scenario starts fresh
        tokenManager.invalidate();

        // 3. Clear all scenario state
        context.clear();

        if (scenario.isFailed()) {
            log.error("✗ FAILED: {}", scenario.getName());
        } else {
            log.info("✓ PASSED: {}", scenario.getName());
        }
    }
}
