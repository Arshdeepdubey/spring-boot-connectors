package com.example.connectors.common.order;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/** Allowed order lifecycle states for the sample business use case. */
public enum OrderStatus {
    NEW,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    private static final Set<String> NAMES = Arrays.stream(values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    public static boolean isValid(String value) {
        return value != null && NAMES.contains(value.trim().toUpperCase());
    }
}
