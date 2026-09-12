package com.example.connectors.resttodb.service;

import com.example.connectors.common.exception.ResourceNotFoundException;
import com.example.connectors.resttodb.dto.OrderResponse;
import com.example.connectors.resttodb.repository.OrderEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrderQueryService {

    private final OrderEntityRepository orderRepository;

    public OrderQueryService(OrderEntityRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Page<OrderResponse> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(OrderResponse::from);
    }

    public OrderResponse findByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("No order found with orderId " + orderId));
    }
}
