package com.farmacity.bff.automation.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnvConfig {
    private String baseUrl;
    private String testUserEmail;
    private String testUserPassword;
    private String testUserDni;
    private String adminApiKey;
}
