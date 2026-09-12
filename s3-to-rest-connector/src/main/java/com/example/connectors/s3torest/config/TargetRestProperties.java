package com.example.connectors.s3torest.config;

import com.example.connectors.common.http.AuthConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * Where this connector delivers each order to. {@link #method} accepts GET, POST, PUT,
 * PATCH or DELETE; most target APIs will use POST (create) or PUT (upsert).
 */
@ConfigurationProperties(prefix = "connector.target.rest")
public class TargetRestProperties {

    /** Full URL of the target endpoint. */
    private String url;

    private HttpMethod method = HttpMethod.POST;

    private Map<String, String> headers = new HashMap<>();

    private AuthConfig auth = new AuthConfig();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public AuthConfig getAuth() {
        return auth;
    }

    public void setAuth(AuthConfig auth) {
        this.auth = auth;
    }
}
