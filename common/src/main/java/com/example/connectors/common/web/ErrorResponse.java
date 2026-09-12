package com.example.connectors.common.web;

import java.time.Instant;
import java.util.List;

/** Consistent error payload returned by every connector's REST endpoints. */
public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<String> details;

    public ErrorResponse(int status, String error, String message, String path, List<String> details) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.details = details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public List<String> getDetails() {
        return details;
    }
}
