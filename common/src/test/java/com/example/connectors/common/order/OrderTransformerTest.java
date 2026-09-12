package com.example.connectors.common.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTransformerTest {

    private final OrderTransformer transformer = new OrderTransformer();

    @Test
    void computesTotalAmountAndDefaultsStatus() {
        OrderRecord input = new OrderRecord();
        input.setOrderId(" ORD-1 ");
        input.setCustomerId(" CUST-1 ");
        input.setProductName(" Widget ");
        input.setQuantity(4);
        input.setPrice(new BigDecimal("2.505"));
        input.setOrderDate("2026-02-01T10:15:30Z");
        input.setStatus(" ");

        OrderRecord result = transformer.transform(input);

        assertThat(result.getOrderId()).isEqualTo("ORD-1");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("10.02");
        assertThat(result.getOrderDate()).isEqualTo("2026-02-01");
        assertThat(result.getStatus()).isEqualTo("NEW");
    }

    @Test
    void normalizesStatusCase() {
        OrderRecord input = new OrderRecord();
        input.setOrderId("ORD-2");
        input.setCustomerId("CUST-2");
        input.setProductName("Gadget");
        input.setQuantity(1);
        input.setPrice(new BigDecimal("5"));
        input.setOrderDate("2026-03-10");
        input.setStatus("shipped");

        OrderRecord result = transformer.transform(input);

        assertThat(result.getStatus()).isEqualTo("SHIPPED");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("5.00");
    }
}
