package com.example.connectors.resttodb.repository;

import com.example.connectors.resttodb.entity.OrderEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class OrderEntityRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderEntityRepository repository;

    @Test
    void findsOrderByOrderId() {
        OrderEntity entity = new OrderEntity();
        entity.setOrderId("ORD-1");
        entity.setCustomerId("CUST-1");
        entity.setProductName("Widget");
        entity.setQuantity(2);
        entity.setPrice(new BigDecimal("9.99"));
        entity.setTotalAmount(new BigDecimal("19.98"));
        entity.setOrderDate(LocalDate.of(2026, 1, 15));
        entity.setStatus("NEW");
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        entityManager.persistAndFlush(entity);

        Optional<OrderEntity> found = repository.findByOrderId("ORD-1");

        assertThat(found).isPresent();
        assertThat(found.get().getCustomerId()).isEqualTo("CUST-1");
        assertThat(found.get().getTotalAmount()).isEqualByComparingTo("19.98");
    }

    @Test
    void returnsEmptyWhenOrderIdNotFound() {
        assertThat(repository.findByOrderId("MISSING")).isEmpty();
    }
}
