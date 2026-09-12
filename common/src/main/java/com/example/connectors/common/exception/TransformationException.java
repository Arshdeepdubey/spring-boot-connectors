package com.example.connectors.common.exception;

/**
 * Raised when the transform or file-conversion stage of a pipeline cannot
 * produce output from an otherwise valid record.
 */
public class TransformationException extends ConnectorException {

    public TransformationException(String message) {
        super(message);
    }

    public TransformationException(String message, Throwable cause) {
        super(message, cause);
    }
}
