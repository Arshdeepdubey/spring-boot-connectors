package com.example.connectors.resttodb.mapper;

import com.example.connectors.common.order.OrderRecord;
import com.example.connectors.resttodb.entity.OrderEntity;

import java.time.Instant;
import java.time.LocalDate;

/** Converts between the shared {@link OrderRecord} DTO and the JPA {@link OrderEntity}. */
public final class OrderEntityMapper {

    private OrderEntityMapper() {
    }

    /** Applies a (validated + transformed) OrderRecord onto a new or existing entity. */
    public static void applyToEntity(OrderRecord record, OrderEntity entity) {
        Instant now = Instant.now();
        boolean isNew = entity.getId() == null;

        entity.setOrderId(record.getOrderId());
        entity.setCustomerId(record.getCustomerId());
        entity.setProductName(record.getProductName());
        entity.setQuantity(record.getQuantity());
        entity.setPrice(record.getPrice());
        entity.setTotalAmount(record.getTotalAmount());
        entity.setOrderDate(LocalDate.parse(record.getOrderDate()));
        entity.setStatus(record.getStatus());
        entity.setUpdatedAt(now);
        if (isNew) {
            entity.setCreatedAt(now);
        }
    }

    public static OrderRecord toRecord(OrderEntity entity) {
        OrderRecord record = new OrderRecord();
        record.setOrderId(entity.getOrderId());
        record.setCustomerId(entity.getCustomerId());
        record.setProductName(entity.getProductName());
        record.setQuantity(entity.getQuantity());
        record.setPrice(entity.getPrice());
        record.setTotalAmount(entity.getTotalAmount());
        record.setOrderDate(entity.getOrderDate().toString());
        record.setStatus(entity.getStatus());
        return record;
    }
}
