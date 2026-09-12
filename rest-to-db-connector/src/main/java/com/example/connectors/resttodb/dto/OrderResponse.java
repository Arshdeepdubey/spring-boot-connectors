package com.example.connectors.resttodb.dto;

import com.example.connectors.resttodb.entity.OrderEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Read-facing view of a persisted order; kept separate from {@link OrderEntity} so the API shape is stable. */
public class OrderResponse {

    private String orderId;
    private String customerId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private LocalDate orderDate;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public static OrderResponse from(OrderEntity entity) {
        OrderResponse response = new OrderResponse();
        response.orderId = entity.getOrderId();
        response.customerId = entity.getCustomerId();
        response.productName = entity.getProductName();
        response.quantity = entity.getQuantity();
        response.price = entity.getPrice();
        response.totalAmount = entity.getTotalAmount();
        response.orderDate = entity.getOrderDate();
        response.status = entity.getStatus();
        response.createdAt = entity.getCreatedAt();
        response.updatedAt = entity.getUpdatedAt();
        return response;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
