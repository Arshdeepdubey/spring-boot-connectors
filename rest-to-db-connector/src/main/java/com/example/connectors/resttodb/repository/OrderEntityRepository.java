package com.example.connectors.resttodb.repository;

import com.example.connectors.resttodb.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderEntityRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByOrderId(String orderId);

    Page<OrderEntity> findAll(Pageable pageable);
}
