package com.example.connectors.resttos3.service;

import com.example.connectors.common.fileconvert.FileConverterServiceImpl;
import com.example.connectors.common.http.RestApiClient;
import com.example.connectors.common.http.RestCallRequest;
import com.example.connectors.common.order.OrderRecord;
import com.example.connectors.common.order.OrderTransformer;
import com.example.connectors.common.order.OrderValidator;
import com.example.connectors.common.pipeline.PipelineResult;
import com.example.connectors.common.pipeline.PipelineStatus;
import com.example.connectors.common.s3.S3StorageService;
import com.example.connectors.resttos3.config.SourceRestProperties;
import com.example.connectors.resttos3.config.TargetS3Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestToS3PipelineServiceTest {

    @Mock
    private RestApiClient restApiClient;

    @Mock
    private S3StorageService s3StorageService;

    private RestToS3PipelineService pipelineService;
    private TargetS3Properties targetProperties;

    @BeforeEach
    void setUp() {
        SourceRestProperties sourceProperties = new SourceRestProperties();
        sourceProperties.setUrl("http://source.test/api/orders");

        targetProperties = new TargetS3Properties();
        targetProperties.setBucket("test-bucket");
        targetProperties.setKeyPrefix("orders/");

        pipelineService = new RestToS3PipelineService(
                restApiClient,
                s3StorageService,
                new FileConverterServiceImpl(),
                new OrderValidator(),
                new OrderTransformer(),
                sourceProperties,
                targetProperties);
    }

    @Test
    void validOrdersAreTransformedAndUploaded() {
        OrderRecord valid = order("ORD-1", 2, "4.50");
        OrderRecord invalid = new OrderRecord(); // missing everything

        when(restApiClient.exchange(any(RestCallRequest.class), eq(OrderRecord[].class)))
                .thenReturn(new OrderRecord[]{valid, invalid});
        when(s3StorageService.upload(anyString(), anyString(), any(byte[].class), anyString()))
                .thenReturn("orders/orders-1.csv");

        PipelineResult result = pipelineService.execute();

        assertThat(result.getRecordsRead()).isEqualTo(2);
        assertThat(result.getRecordsValid()).isEqualTo(1);
        assertThat(result.getRecordsInvalid()).isEqualTo(1);
        assertThat(result.getRecordsDelivered()).isEqualTo(1);
        assertThat(result.getStatus()).isEqualTo(PipelineStatus.PARTIAL_SUCCESS);
        assertThat(result.getTargetLocation()).startsWith("s3://test-bucket/orders/");
        verify(s3StorageService).upload(eq("test-bucket"), anyString(), any(byte[].class), anyString());
    }

    @Test
    void noSourceRecordsMeansNothingUploaded() {
        when(restApiClient.exchange(any(RestCallRequest.class), eq(OrderRecord[].class)))
                .thenReturn(new OrderRecord[0]);

        PipelineResult result = pipelineService.execute();

        assertThat(result.getRecordsRead()).isZero();
        assertThat(result.getRecordsDelivered()).isZero();
        assertThat(result.getStatus()).isEqualTo(PipelineStatus.SUCCESS);
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
