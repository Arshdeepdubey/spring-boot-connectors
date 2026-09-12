package com.example.connectors.common.order;

import com.example.connectors.common.validate.ValidationResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderValidatorTest {

    private final OrderValidator validator = new OrderValidator();

    private OrderRecord validOrder() {
        OrderRecord order = new OrderRecord();
        order.setOrderId("ORD-1");
        order.setCustomerId("CUST-1");
        order.setProductName("Widget");
        order.setQuantity(3);
        order.setPrice(new BigDecimal("9.99"));
        order.setOrderDate("2026-01-15");
        order.setStatus("NEW");
        return order;
    }

    @Test
    void validOrderPassesValidation() {
        ValidationResult result = validator.validate(validOrder());
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void missingRequiredFieldsAreReported() {
        OrderRecord order = new OrderRecord();
        ValidationResult result = validator.validate(order);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains(
                "orderId is required", "customerId is required", "productName is required",
                "quantity must be a positive integer", "price must be zero or greater", "orderDate is required");
    }

    @Test
    void negativeQuantityIsRejected() {
        OrderRecord order = validOrder();
        order.setQuantity(-1);
        ValidationResult result = validator.validate(order);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("quantity must be a positive integer");
    }

    @Test
    void unknownStatusIsRejected() {
        OrderRecord order = validOrder();
        order.setStatus("BOGUS");
        ValidationResult result = validator.validate(order);
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void unparseableOrderDateIsRejected() {
        OrderRecord order = validOrder();
        order.setOrderDate("not-a-date");
        ValidationResult result = validator.validate(order);
        assertThat(result.isValid()).isFalse();
    }
}
