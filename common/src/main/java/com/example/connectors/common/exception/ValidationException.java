package com.example.connectors.common.exception;

import java.util.List;

/**
 * Raised when incoming records fail the connector's business validation rules.
 */
public class ValidationException extends ConnectorException {

    private final List<String> errors;

    public ValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
