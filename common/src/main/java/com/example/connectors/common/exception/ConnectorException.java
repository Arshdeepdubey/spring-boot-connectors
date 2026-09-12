package com.example.connectors.common.exception;

/**
 * Base, unchecked exception for every error raised inside a connector pipeline
 * (source read, validation, transformation, file conversion or target delivery).
 * Kept as a single hierarchy so {@code GlobalExceptionHandler} can translate any
 * connector failure into a consistent HTTP error response.
 */
public class ConnectorException extends RuntimeException {

    public ConnectorException(String message) {
        super(message);
    }

    public ConnectorException(String message, Throwable cause) {
        super(message, cause);
    }
}
