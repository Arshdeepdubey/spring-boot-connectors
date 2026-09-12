package com.example.connectors.common.validate;

/** Business validation contract applied to each record a connector reads from its source. */
public interface RecordValidator<T> {

    ValidationResult validate(T record);
}
