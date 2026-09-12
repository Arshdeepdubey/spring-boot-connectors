package com.example.connectors.resttodb.service;

import com.example.connectors.common.http.RestApiClient;
import com.example.connectors.common.http.RestCallRequest;
import com.example.connectors.common.order.OrderRecord;
import com.example.connectors.common.order.OrderTransformer;
import com.example.connectors.common.order.OrderValidator;
import com.example.connectors.common.pipeline.PipelineResult;
import com.example.connectors.common.validate.ValidationResult;
import com.example.connectors.resttodb.config.SourceRestProperties;
import com.example.connectors.resttodb.entity.OrderEntity;
import com.example.connectors.resttodb.mapper.OrderEntityMapper;
import com.example.connectors.resttodb.repository.OrderEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrates the full rest-to-db pipeline: fetch orders from the configured source
 * REST API, validate and transform each one, then upsert it into Postgres (an existing
 * orderId is updated in place, a new one is inserted). {@code orderRepository.save(...)}
 * is itself transactional per call (Spring Data's {@code SimpleJpaRepository}), and since
 * this loop does not wrap itself in a single outer transaction, one row failing to persist
 * cannot roll back rows already committed earlier in the same batch.
 */
@Service
public class RestToDbPipelineService {

    private static final Logger log = LoggerFactory.getLogger(RestToDbPipelineService.class);

    private final RestApiClient restApiClient;
    private final OrderEntityRepository orderRepository;
    private final OrderValidator orderValidator;
    private final OrderTransformer orderTransformer;
    private final SourceRestProperties sourceProperties;

    public RestToDbPipelineService(RestApiClient restApiClient,
                                    OrderEntityRepository orderRepository,
                                    OrderValidator orderValidator,
                                    OrderTransformer orderTransformer,
                                    SourceRestProperties sourceProperties) {
        this.restApiClient = restApiClient;
        this.orderRepository = orderRepository;
        this.orderValidator = orderValidator;
        this.orderTransformer = orderTransformer;
        this.sourceProperties = sourceProperties;
    }

    public PipelineResult execute() {
        PipelineResult result = new PipelineResult();
        result.setSourceLocation(sourceProperties.getMethod() + " " + sourceProperties.getUrl());

        List<OrderRecord> sourceOrders = fetchOrders();
        result.setRecordsRead(sourceOrders.size());

        int persisted = 0;
        for (OrderRecord order : sourceOrders) {
            ValidationResult validation = orderValidator.validate(order);
            if (!validation.isValid()) {
                result.setRecordsInvalid(result.getRecordsInvalid() + 1);
                String orderRef = order.getOrderId() == null ? "<unknown>" : order.getOrderId();
                validation.getErrors().forEach(err -> result.addError("order " + orderRef + ": " + err));
                continue;
            }
            result.setRecordsValid(result.getRecordsValid() + 1);

            OrderRecord transformed = orderTransformer.transform(order);
            if (persistOne(transformed, result)) {
                persisted++;
            }
        }

        result.setRecordsDelivered(persisted);
        result.setTargetLocation("postgres:orders");
        result.finish();
        log.info("rest-to-db pipeline finished: read={}, valid={}, invalid={}, persisted={}, failed={}, status={}",
                result.getRecordsRead(), result.getRecordsValid(), result.getRecordsInvalid(),
                persisted, result.getRecordsFailed(), result.getStatus());
        return result;
    }

    private boolean persistOne(OrderRecord order, PipelineResult result) {
        try {
            OrderEntity entity = orderRepository.findByOrderId(order.getOrderId()).orElseGet(OrderEntity::new);
            OrderEntityMapper.applyToEntity(order, entity);
            orderRepository.save(entity);
            return true;
        } catch (RuntimeException ex) {
            result.incrementFailed();
            result.addError("order " + order.getOrderId() + ": persistence failed - " + ex.getMessage());
            log.warn("Failed to persist order {}: {}", order.getOrderId(), ex.getMessage());
            return false;
        }
    }

    private List<OrderRecord> fetchOrders() {
        RestCallRequest request = RestCallRequest.builder()
                .url(sourceProperties.getUrl())
                .method(sourceProperties.getMethod())
                .headers(sourceProperties.getHeaders())
                .auth(sourceProperties.getAuth())
                .build();
        OrderRecord[] orders = restApiClient.exchange(request, OrderRecord[].class);
        return orders == null ? List.of() : List.of(orders);
    }
}
