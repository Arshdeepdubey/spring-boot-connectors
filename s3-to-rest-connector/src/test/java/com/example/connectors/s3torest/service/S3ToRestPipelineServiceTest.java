package com.example.connectors.s3torest.service;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

class S3ToRestPipelineServiceTest {

    public static class MockS3StorageService implements S3StorageService {
        public byte[] contentToReturn = new byte[0];

        @Override
        public String upload(String bucket, String key, byte[] content, String contentType) {
            return key;
        }

        @Override
        public byte[] download(String bucket, String key) {
            return contentToReturn;
        }

        @Override
        public List<String> listObjectKeys(String bucket, String prefix) {
            return List.of();
        }

        @Override
        public boolean exists(String bucket, String key) {
            return true;
        }
    }

    public static class MockRestApiClient implements RestApiClient {
        public Object responseToReturn;
        public RuntimeException exceptionToThrow;

        @Override
        public <T> T exchange(RestCallRequest request, Class<T> responseType) throws ExternalServiceException {
            if (exceptionToThrow != null) {
                if (exceptionToThrow instanceof ExternalServiceException) {
                    throw (ExternalServiceException) exceptionToThrow;
                } else {
                    throw exceptionToThrow;
                }
            }
            return responseType.cast(responseToReturn);
        }
    }

    private S3ToRestPipelineService pipelineService;
    private SourceS3Properties sourceProperties;
    private MockS3StorageService mockS3Service;
    private MockRestApiClient mockRestClient;

    @BeforeEach
    void setUp() {
        sourceProperties = new SourceS3Properties();
        sourceProperties.setBucket("test-bucket");
        sourceProperties.setKey("orders/orders-1.csv");
        sourceProperties.setFileFormat(FileFormat.CSV);

        TargetRestProperties targetProperties = new TargetRestProperties();
        targetProperties.setUrl("http://target.test/api/orders/relay");

        mockS3Service = new MockS3StorageService();
        mockRestClient = new MockRestApiClient();

        pipelineService = new S3ToRestPipelineService(
                mockS3Service, mockRestClient, new FileConverterServiceImpl(),
                new OrderValidator(), new OrderTransformer(), sourceProperties, targetProperties);
    }

    @Test
    void deliversValidOrdersAndSkipsInvalidOnes() {
        OrderRecord valid = order("ORD-1", 2, "4.50");
        OrderRecord invalid = order("ORD-2", -1, "1.00"); // invalid quantity
        byte[] csv = new FileConverterServiceImpl().convert(
                List.of(OrderMapper.toRow(valid), OrderMapper.toRow(invalid)), FileFormat.CSV, OrderMapper.CSV_COLUMNS);

        mockS3Service.contentToReturn = csv;
        mockRestClient.responseToReturn = "OK";

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
        byte[] csv = new FileConverterServiceImpl().convert(List.of(OrderMapper.toRow(valid)), FileFormat.CSV, OrderMapper.CSV_COLUMNS);

        mockS3Service.contentToReturn = csv;
        mockRestClient.exceptionToThrow = new ExternalServiceException("target API returned 500");

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
        
        // Create a new mock that returns list of keys
        class ListingS3StorageService extends MockS3StorageService {
            @Override
            public List<String> listObjectKeys(String bucket, String prefix) {
                return List.of("orders/orders-100.csv", "orders/orders-200.csv");
            }
        }
        
        mockS3Service = new ListingS3StorageService();
        mockS3Service.contentToReturn = new FileConverterServiceImpl().convert(List.of(), FileFormat.CSV, OrderMapper.CSV_COLUMNS);
        
        pipelineService = new S3ToRestPipelineService(
                mockS3Service, mockRestClient, new FileConverterServiceImpl(),
                new OrderValidator(), new OrderTransformer(), sourceProperties, new TargetRestProperties() {{
                    setUrl("http://target.test/api/orders/relay");
                }});

        mockRestClient.responseToReturn = "OK";

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
