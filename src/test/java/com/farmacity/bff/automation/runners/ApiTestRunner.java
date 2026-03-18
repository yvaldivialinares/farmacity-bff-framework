package com.farmacity.bff.automation.runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Full regression suite runner.
 *
 * Run with:
 *   mvn test -Denv=qa
 *   mvn test -Denv=uat -Dcucumber.filter.tags=@regression
 *
 * Excludes @wip tags (work-in-progress scenarios not yet ready to run).
 */
@Suite
@IncludeEngines("cucumber")
@ConfigurationParameter(
        key   = PLUGIN_PROPERTY_NAME,
        value = "pretty, "
                + "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm, "
                + "json:target/cucumber-reports/regression-report.json, "
                + "html:target/cucumber-reports/regression-report.html"
)
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME, value = "src/test/resources/features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME,     value = "com.farmacity.bff.automation")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @wip")
public class ApiTestRunner {}
