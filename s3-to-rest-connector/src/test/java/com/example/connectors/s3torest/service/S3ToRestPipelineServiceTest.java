package com.example.connectors.s3torest.service;

import com.example.connectors.common.exception.ExternalServiceException;
import com.example.connectors.common.fileconvert.FileConverterServiceImpl;
import com.example.connectors.common.fileconvert.FileFormat;
import com.example.connectors.common.http.RestApiClient;
import com.example.connectors.common.http.RestCallRequest;
import com.example.connectors.common.order.OrderMapper;
import com.example.connectors.common.order.OrderRecord;
import com.example.connectors.common.order.OrderTransformer;
import com.example.connectors.common.order.OrderValidator;
import com.example.connectors.common.pipeline.PipelineResult;
import com.example.connectors.common.pipeline.PipelineStatus;
import com.example.connectors.common.s3.S3StorageService;
import com.example.connectors.s3torest.config.SourceS3Properties;
import com.example.connectors.s3torest.config.TargetRestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ToRestPipelineServiceTest {

    @Mock
    private S3StorageService s3StorageService;

    @Mock
    private RestApiClient restApiClient;

    private final FileConverterServiceImpl fileConverterService = new FileConverterServiceImpl();

    private S3ToRestPipelineService pipelineService;
    private SourceS3Properties sourceProperties;

    @BeforeEach
    void setUp() {
        sourceProperties = new SourceS3Properties();
        sourceProperties.setBucket("test-bucket");
        sourceProperties.setKey("orders/orders-1.csv");
        sourceProperties.setFileFormat(FileFormat.CSV);

        TargetRestProperties targetProperties = new TargetRestProperties();
        targetProperties.setUrl("http://target.test/api/orders/relay");

        pipelineService = new S3ToRestPipelineService(
                s3StorageService, restApiClient, fileConverterService,
                new OrderValidator(), new OrderTransformer(), sourceProperties, targetProperties);
    }

    @Test
    void deliversValidOrdersAndSkipsInvalidOnes() {
        OrderRecord valid = order("ORD-1", 2, "4.50");
        OrderRecord invalid = order("ORD-2", -1, "1.00"); // invalid quantity
        byte[] csv = fileConverterService.convert(
                List.of(OrderMapper.toRow(valid), OrderMapper.toRow(invalid)), FileFormat.CSV, OrderMapper.CSV_COLUMNS);

        when(s3StorageService.download("test-bucket", "orders/orders-1.csv")).thenReturn(csv);
        when(restApiClient.exchange(any(RestCallRequest.class), eq(String.class))).thenReturn("OK");

        PipelineResult result = pipelineService.execute();

        assertThat(result.getRecordsRead()).isEqualTo(2);
        assertThat(result.getRecordsValid()).isEqualTo(1);
        assertThat(result.getRecordsInvalid()).isEqualTo(1);
        assertThat(result.getRecordsDelivered()).isEqualTo(1);
        assertThat(result.getStatus()).isEqualTo(PipelineStatus.PARTIAL_SUCCESS);
    }

    @Test
    void deliveryFailureIsRecordedNotThrown() {
        OrderRecord valid = order("ORD-1", 2, "4.50");
        byte[] csv = fileConverterService.convert(List.of(OrderMapper.toRow(valid)), FileFormat.CSV, OrderMapper.CSV_COLUMNS);

        when(s3StorageService.download("test-bucket", "orders/orders-1.csv")).thenReturn(csv);
        when(restApiClient.exchange(any(RestCallRequest.class), eq(String.class)))
                .thenThrow(new ExternalServiceException("target API returned 500"));

        PipelineResult result = pipelineService.execute();

        assertThat(result.getRecordsValid()).isEqualTo(1);
        assertThat(result.getRecordsFailed()).isEqualTo(1);
        assertThat(result.getRecordsDelivered()).isZero();
        assertThat(result.getStatus()).isEqualTo(PipelineStatus.FAILED);
        assertThat(result.getErrors()).anyMatch(e -> e.contains("delivery failed"));
    }

    @Test
    void resolvesLatestKeyWhenNoExplicitKeyConfigured() {
        sourceProperties.setKey(null);
        sourceProperties.setKeyPrefix("orders/");
        when(s3StorageService.listObjectKeys("test-bucket", "orders/"))
                .thenReturn(List.of("orders/orders-100.csv", "orders/orders-200.csv"));
        when(s3StorageService.download("test-bucket", "orders/orders-200.csv"))
                .thenReturn(fileConverterService.convert(List.of(), FileFormat.CSV, OrderMapper.CSV_COLUMNS));

        PipelineResult result = pipelineService.execute();

        assertThat(result.getSourceLocation()).isEqualTo("s3://test-bucket/orders/orders-200.csv");
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
