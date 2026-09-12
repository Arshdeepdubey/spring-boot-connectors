package com.example.connectors.common.order;

import com.example.connectors.common.transform.RecordTransformer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Normalizes a validated order: computes the total amount, defaults and
 * upper-cases the status, and canonicalizes the order date to yyyy-MM-dd.
 * This is the one "business rule" transformation shared by every connector
 * that touches orders.
 */
@Component
public class OrderTransformer implements RecordTransformer<OrderRecord, OrderRecord> {

    @Override
    public OrderRecord transform(OrderRecord input) {
        OrderRecord out = new OrderRecord();
        out.setOrderId(input.getOrderId().trim());
        out.setCustomerId(input.getCustomerId().trim());
        out.setProductName(input.getProductName().trim());
        out.setQuantity(input.getQuantity());
        out.setPrice(input.getPrice().setScale(2, RoundingMode.HALF_UP));
        out.setTotalAmount(input.getPrice()
                .multiply(BigDecimal.valueOf(input.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP));
        out.setOrderDate(canonicalDate(input.getOrderDate()));
        out.setStatus(input.getStatus() == null || input.getStatus().isBlank()
                ? OrderStatus.NEW.name()
                : input.getStatus().trim().toUpperCase());
        return out;
    }

    private String canonicalDate(String rawDate) {
        String datePart = rawDate.length() >= 10 ? rawDate.substring(0, 10) : rawDate;
        return LocalDate.parse(datePart).toString();
    }
}
