package com.example.connectors.common.web;

import java.time.Instant;

/** Thin, consistent success envelope used by every connector controller. */
public class ApiResponse<T> {

    private final Instant timestamp;
    private final String status;
    private final T data;

    private ApiResponse(String status, T data) {
        this.timestamp = Instant.now();
        this.status = status;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("OK", data);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }
}
