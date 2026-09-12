package com.example.connectors.common.exception;

/**
 * Raised when a downstream system the connector depends on (a source/target REST
 * API, AWS S3, or the database) fails, times out, or returns an error status.
 */
public class ExternalServiceException extends ConnectorException {

    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
