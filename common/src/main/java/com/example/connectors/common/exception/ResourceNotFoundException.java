package com.example.connectors.common.exception;

/** Raised when a requested resource (e.g. an order by id) does not exist. */
public class ResourceNotFoundException extends ConnectorException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
