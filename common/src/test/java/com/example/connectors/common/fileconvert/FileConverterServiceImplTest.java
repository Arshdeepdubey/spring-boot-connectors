package com.example.connectors.common.fileconvert;

import com.example.connectors.common.order.OrderMapper;
import com.example.connectors.common.order.OrderRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FileConverterServiceImplTest {

    private final FileConverterServiceImpl converter = new FileConverterServiceImpl();

    @Test
    void roundTripsCsv() {
        OrderRecord order = new OrderRecord("ORD-1", "CUST-1", "Widget", 2,
                new BigDecimal("9.99"), new BigDecimal("19.98"), "2026-01-15", "NEW");
        List<Map<String, Object>> rows = List.of(OrderMapper.toRow(order));

        byte[] csv = converter.convert(rows, FileFormat.CSV, OrderMapper.CSV_COLUMNS);
        assertThat(new String(csv, StandardCharsets.UTF_8)).contains("orderId", "ORD-1", "Widget");

        List<Map<String, Object>> parsed = converter.parse(csv, FileFormat.CSV);
        assertThat(parsed).hasSize(1);
        OrderRecord roundTripped = OrderMapper.fromRow(parsed.get(0));
        assertThat(roundTripped.getOrderId()).isEqualTo("ORD-1");
        assertThat(roundTripped.getTotalAmount()).isEqualByComparingTo("19.98");
    }

    @Test
    void roundTripsJson() {
        OrderRecord order = new OrderRecord("ORD-2", "CUST-2", "Gadget", 1,
                new BigDecimal("5.00"), new BigDecimal("5.00"), "2026-02-01", "SHIPPED");
        List<Map<String, Object>> rows = List.of(OrderMapper.toRow(order));

        byte[] json = converter.convert(rows, FileFormat.JSON, OrderMapper.CSV_COLUMNS);
        List<Map<String, Object>> parsed = converter.parse(json, FileFormat.JSON);

        assertThat(parsed).hasSize(1);
        assertThat(parsed.get(0).get("orderId")).isEqualTo("ORD-2");
    }
}
