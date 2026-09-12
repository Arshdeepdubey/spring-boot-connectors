package com.example.connectors.common.http;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Shared tuning knobs for the outbound REST client used by all connectors. */
@ConfigurationProperties(prefix = "connector.http")
public class HttpClientProperties {

    /** Connection timeout, in milliseconds. */
    private int connectTimeoutMs = 5000;

    /** Read/response timeout, in milliseconds. */
    private int readTimeoutMs = 10000;

    /** Number of retry attempts for transient failures (timeouts, 5xx, connection resets). */
    private int maxRetries = 2;

    /** Delay between retry attempts, in milliseconds (simple fixed backoff). */
    private long retryBackoffMs = 500;

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getRetryBackoffMs() {
        return retryBackoffMs;
    }

    public void setRetryBackoffMs(long retryBackoffMs) {
        this.retryBackoffMs = retryBackoffMs;
    }
}
