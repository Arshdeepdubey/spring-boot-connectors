package com.example.connectors.common.order;

import com.example.connectors.common.validate.RecordValidator;
import com.example.connectors.common.validate.ValidationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic business validation rules for an incoming order: required fields present,
 * quantity positive, price non-negative, order date parseable, and (if given) a
 * recognised status. Kept deliberately simple per the brief.
 */
@Component
public class OrderValidator implements RecordValidator<OrderRecord> {

    @Override
    public ValidationResult validate(OrderRecord order) {
        List<String> errors = new ArrayList<>();

        if (isBlank(order.getOrderId())) {
            errors.add("orderId is required");
        }
        if (isBlank(order.getCustomerId())) {
            errors.add("customerId is required");
        }
        if (isBlank(order.getProductName())) {
            errors.add("productName is required");
        }
        if (order.getQuantity() == null || order.getQuantity() <= 0) {
            errors.add("quantity must be a positive integer");
        }
        if (order.getPrice() == null || order.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("price must be zero or greater");
        }
        if (isBlank(order.getOrderDate())) {
            errors.add("orderDate is required");
        } else if (!isParseableDate(order.getOrderDate())) {
            errors.add("orderDate must be an ISO-8601 date (yyyy-MM-dd) or date-time");
        }
        if (!isBlank(order.getStatus()) && !OrderStatus.isValid(order.getStatus())) {
            errors.add("status must be one of NEW, PROCESSING, SHIPPED, DELIVERED, CANCELLED");
        }

        return errors.isEmpty() ? ValidationResult.valid() : ValidationResult.invalid(errors);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isParseableDate(String value) {
        try {
            LocalDate.parse(value.length() >= 10 ? value.substring(0, 10) : value);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }
}
