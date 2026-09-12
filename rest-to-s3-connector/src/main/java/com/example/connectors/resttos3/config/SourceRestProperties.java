package com.example.connectors.resttos3.config;

import com.example.connectors.common.http.AuthConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * Points this connector at the upstream REST API it reads orders from.
 * {@link #method} accepts GET, POST, PUT, PATCH or DELETE so the same connector can be
 * pointed at any style of source endpoint (a plain GET listing, or a POST-based "search" API).
 */
@ConfigurationProperties(prefix = "connector.source.rest")
public class SourceRestProperties {

    /** Full URL of the source endpoint that returns a JSON array of orders. */
    private String url;

    /** HTTP method to call the source endpoint with. Defaults to GET. */
    private HttpMethod method = HttpMethod.GET;

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
