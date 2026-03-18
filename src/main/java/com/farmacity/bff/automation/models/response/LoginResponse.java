package com.farmacity.bff.automation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {

    @JsonProperty("token")
    private String token;

    @JsonProperty("hasTUF")
    private boolean hasTUF;

    @JsonProperty("user")
    private UserInfo user;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserInfo {

        @JsonProperty("name")
        private String name;

        @JsonProperty("lastname")
        private String lastname;

        @JsonProperty("dni")
        private String dni;

        @JsonProperty("email")
        private String email;
    }
}
