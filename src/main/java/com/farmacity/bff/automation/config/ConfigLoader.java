package com.farmacity.bff.automation.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton that reads environment config once on startup.
 *
 * Active environment is selected via -Denv=qa|dev|uat|prod (defaults to "qa").
 * Each environment maps to src/test/resources/config/{env}.properties.
 */
public class ConfigLoader {

    private static volatile ConfigLoader instance;

    private final EnvConfig config = new EnvConfig();

    private ConfigLoader() {
        String env = System.getProperty("env", "qa");
        Properties props = loadProperties("config/" + env + ".properties");

        config.setBaseUrl(Environment.fromString(env).getBaseUrl());
        config.setTestUserEmail(props.getProperty("test.user.email"));
        config.setTestUserPassword(props.getProperty("test.user.password"));
        config.setTestUserDni(props.getProperty("test.user.dni"));
        config.setAdminApiKey(props.getProperty("admin.api.key", ""));
    }

    public static ConfigLoader getInstance() {
        if (instance == null) {
            synchronized (ConfigLoader.class) {
                if (instance == null) instance = new ConfigLoader();
            }
        }
        return instance;
    }

    public String getBaseUrl()          { return config.getBaseUrl(); }
    public String getTestUserEmail()    { return config.getTestUserEmail(); }
    public String getTestUserPassword() { return config.getTestUserPassword(); }
    public String getTestUserDni()      { return config.getTestUserDni(); }
    public String getAdminApiKey()      { return config.getAdminApiKey(); }

    private Properties loadProperties(String resourcePath) {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Config file not found in classpath: " + resourcePath);
            }
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config: " + resourcePath, e);
        }
        return props;
    }
}
