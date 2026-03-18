package com.farmacity.bff.automation.runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Smoke test runner — only executes scenarios tagged @smoke.
 *
 * Designed for fast feedback on critical paths (~10 scenarios, < 2 min).
 * Run after every deployment to verify the environment is healthy.
 *
 * Run with:
 *   mvn test -Denv=qa  -Dtest=SmokeTestRunner
 *   mvn test -Denv=uat -Dtest=SmokeTestRunner
 */
@Suite
@IncludeEngines("cucumber")
@ConfigurationParameter(
        key   = PLUGIN_PROPERTY_NAME,
        value = "pretty, "
                + "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm, "
                + "json:target/cucumber-reports/smoke-report.json"
)
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME,    value = "src/test/resources/features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME,        value = "com.farmacity.bff.automation")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@smoke")
public class SmokeTestRunner {}
