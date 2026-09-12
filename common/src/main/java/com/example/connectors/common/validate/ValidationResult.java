package com.example.connectors.common.validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Outcome of validating a single record: either valid, or valid=false with the reasons why. */
public final class ValidationResult {

    private static final ValidationResult VALID = new ValidationResult(true, Collections.emptyList());

    private final boolean valid;
    private final List<String> errors;

    private ValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = errors;
    }

    public static ValidationResult valid() {
        return VALID;
    }

    public static ValidationResult invalid(List<String> errors) {
        return new ValidationResult(false, new ArrayList<>(errors));
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getErrors() {
        return errors;
    }
}
