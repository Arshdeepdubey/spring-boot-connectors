package com.example.connectors.common.order;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts between {@link OrderRecord} and the flat {@code Map<String,Object>} row
 * shape the generic {@code FileConverterService} (CSV/JSON) operates on.
 */
public final class OrderMapper {

    /** Column order used for the CSV representation of an order. */
    public static final List<String> CSV_COLUMNS = List.of(
            "orderId", "customerId", "productName", "quantity", "price", "totalAmount", "orderDate", "status");

    private OrderMapper() {
    }

    public static Map<String, Object> toRow(OrderRecord order) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("orderId", order.getOrderId());
        row.put("customerId", order.getCustomerId());
        row.put("productName", order.getProductName());
        row.put("quantity", order.getQuantity());
        row.put("price", order.getPrice());
        row.put("totalAmount", order.getTotalAmount());
        row.put("orderDate", order.getOrderDate());
        row.put("status", order.getStatus());
        return row;
    }

    public static OrderRecord fromRow(Map<String, Object> row) {
        OrderRecord order = new OrderRecord();
        order.setOrderId(asString(row.get("orderId")));
        order.setCustomerId(asString(row.get("customerId")));
        order.setProductName(asString(row.get("productName")));
        order.setQuantity(asInteger(row.get("quantity")));
        order.setPrice(asBigDecimal(row.get("price")));
        order.setTotalAmount(asBigDecimal(row.get("totalAmount")));
        order.setOrderDate(asString(row.get("orderDate")));
        order.setStatus(asString(row.get("status")));
        return order;
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private static Integer asInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        String s = value.toString().trim();
        return s.isEmpty() ? null : Integer.valueOf(s);
    }

    private static BigDecimal asBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        String s = value.toString().trim();
        return s.isEmpty() ? null : new BigDecimal(s);
    }
}
