package com.example.connectors.common.http;

/**
 * Authentication details for a single REST endpoint. Which fields matter depends on
 * {@link #type}: BASIC uses username/password, API_KEY uses apiKeyHeader/apiKeyValue,
 * BEARER uses bearerToken. Leave type as NONE (the default) for unauthenticated APIs.
 */
public class AuthConfig {

    private AuthType type = AuthType.NONE;
    private String username;
    private String password;
    private String apiKeyHeader = "X-API-Key";
    private String apiKeyValue;
    private String bearerToken;

    public AuthType getType() {
        return type;
    }

    public void setType(AuthType type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApiKeyHeader() {
        return apiKeyHeader;
    }

    public void setApiKeyHeader(String apiKeyHeader) {
        this.apiKeyHeader = apiKeyHeader;
    }

    public String getApiKeyValue() {
        return apiKeyValue;
    }

    public void setApiKeyValue(String apiKeyValue) {
        this.apiKeyValue = apiKeyValue;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }
}
