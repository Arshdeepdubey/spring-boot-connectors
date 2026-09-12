package com.example.connectors.common.http;

import org.springframework.http.HttpMethod;

import java.util.Map;

/**
 * Everything needed to make one outbound call to a source or target REST API.
 * {@link #method} accepts any of GET, POST, PUT, PATCH or DELETE, which is what
 * lets one client implementation serve every connector.
 */
public class RestCallRequest {

    private final String url;
    private final HttpMethod method;
    private final Map<String, String> headers;
    private final Object body;
    private final AuthConfig auth;

    private RestCallRequest(Builder b) {
        this.url = b.url;
        this.method = b.method;
        this.headers = b.headers;
        this.body = b.body;
        this.auth = b.auth;
    }

    public String getUrl() {
        return url;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Object getBody() {
        return body;
    }

    public AuthConfig getAuth() {
        return auth;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String url;
        private HttpMethod method = HttpMethod.GET;
        private Map<String, String> headers = Map.of();
        private Object body;
        private AuthConfig auth = new AuthConfig();

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder body(Object body) {
            this.body = body;
            return this;
        }

        public Builder auth(AuthConfig auth) {
            this.auth = auth;
            return this;
        }

        public RestCallRequest build() {
            return new RestCallRequest(this);
        }
    }
}
