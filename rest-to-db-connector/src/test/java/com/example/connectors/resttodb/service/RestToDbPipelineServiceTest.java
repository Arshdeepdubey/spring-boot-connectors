package com.example.connectors.resttodb.service;

import com.example.connectors.common.http.RestApiClient;
import com.example.connectors.common.http.RestCallRequest;
import com.example.connectors.common.order.OrderRecord;
import com.example.connectors.common.order.OrderTransformer;
import com.example.connectors.common.order.OrderValidator;
import com.example.connectors.common.pipeline.PipelineResult;
import com.example.connectors.common.pipeline.PipelineStatus;
import com.example.connectors.resttodb.config.SourceRestProperties;
import com.example.connectors.resttodb.entity.OrderEntity;
import com.example.connectors.resttodb.repository.OrderEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestToDbPipelineServiceTest {

    @Mock
    private RestApiClient restApiClient;

    @Mock
    private OrderEntityRepository orderRepository;

    private RestToDbPipelineService pipelineService;

    @BeforeEach
    void setUp() {
        SourceRestProperties sourceProperties = new SourceRestProperties();
        sourceProperties.setUrl("http://source.test/api/orders");

        pipelineService = new RestToDbPipelineService(
                restApiClient, orderRepository, new OrderValidator(), new OrderTransformer(), sourceProperties);
    }

    @Test
    void newOrderIsInsertedAndExistingOrderIsUpdated() {
        OrderRecord newOrder = order("ORD-NEW", 2, "5.00");
        OrderRecord existingOrder = order("ORD-EXISTING", 1, "10.00");

        when(restApiClient.exchange(any(RestCallRequest.class), eq(OrderRecord[].class)))
                .thenReturn(new OrderRecord[]{newOrder, existingOrder});
        when(orderRepository.findByOrderId("ORD-NEW")).thenReturn(Optional.empty());
        OrderEntity existingEntity = new OrderEntity();
        existingEntity.setId(42L);
        existingEntity.setOrderId("ORD-EXISTING");
        when(orderRepository.findByOrderId("ORD-EXISTING")).thenReturn(Optional.of(existingEntity));

        PipelineResult result = pipelineService.execute();

        assertThat(result.getRecordsRead()).isEqualTo(2);
        assertThat(result.getRecordsValid()).isEqualTo(2);
        assertThat(result.getRecordsDelivered()).isEqualTo(2);
        assertThat(result.getStatus()).isEqualTo(PipelineStatus.SUCCESS);
        verify(orderRepository, org.mockito.Mockito.times(2)).save(any(OrderEntity.class));
    }

    @Test
    void invalidOrdersAreSkippedAndReported() {
        OrderRecord invalid = new OrderRecord();
        when(restApiClient.exchange(any(RestCallRequest.class), eq(OrderRecord[].class)))
                .thenReturn(new OrderRecord[]{invalid});

        PipelineResult result = pipelineService.execute();

        assertThat(result.getRecordsInvalid()).isEqualTo(1);
        assertThat(result.getRecordsDelivered()).isZero();
        assertThat(result.getStatus()).isEqualTo(PipelineStatus.FAILED);
    }

    @Test
    void persistenceFailureIsRecordedNotThrown() {
        OrderRecord valid = order("ORD-1", 1, "3.00");
        when(restApiClient.exchange(any(RestCallRequest.class), eq(OrderRecord[].class)))
                .thenReturn(new OrderRecord[]{valid});
        when(orderRepository.findByOrderId("ORD-1")).thenReturn(Optional.empty());
        when(orderRepository.save(any(OrderEntity.class))).thenThrow(new RuntimeException("constraint violation"));

        PipelineResult result = pipelineService.execute();

        assertThat(result.getRecordsFailed()).isEqualTo(1);
        assertThat(result.getRecordsDelivered()).isZero();
        assertThat(result.getStatus()).isEqualTo(PipelineStatus.FAILED);
    }

    private OrderRecord order(String id, int qty, String price) {
        OrderRecord order = new OrderRecord();
        order.setOrderId(id);
        order.setCustomerId("CUST-1");
        order.setProductName("Widget");
        order.setQuantity(qty);
        order.setPrice(new BigDecimal(price));
        order.setOrderDate("2026-01-15");
        order.setStatus("NEW");
        return order;
    }
}
