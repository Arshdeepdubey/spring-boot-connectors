package com.example.connectors.resttos3.service;

import com.example.connectors.common.fileconvert.FileConverterService;
import com.example.connectors.common.http.RestApiClient;
import com.example.connectors.common.http.RestCallRequest;
import com.example.connectors.common.order.OrderMapper;
import com.example.connectors.common.order.OrderRecord;
import com.example.connectors.common.order.OrderTransformer;
import com.example.connectors.common.order.OrderValidator;
import com.example.connectors.common.pipeline.PipelineResult;
import com.example.connectors.common.s3.S3StorageService;
import com.example.connectors.common.validate.ValidationResult;
import com.example.connectors.resttos3.config.SourceRestProperties;
import com.example.connectors.resttos3.config.TargetS3Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates the full rest-to-s3 pipeline: fetch orders from the configured source
 * REST API, validate each one, transform the valid ones, convert the batch to a file
 * (CSV or JSON), and upload it to the configured S3 bucket. Invalid records are
 * skipped and reported rather than failing the whole run.
 */
@Service
public class RestToS3PipelineService {

    private static final Logger log = LoggerFactory.getLogger(RestToS3PipelineService.class);

    private final RestApiClient restApiClient;
    private final S3StorageService s3StorageService;
    private final FileConverterService fileConverterService;
    private final OrderValidator orderValidator;
    private final OrderTransformer orderTransformer;
    private final SourceRestProperties sourceProperties;
    private final TargetS3Properties targetProperties;

    public RestToS3PipelineService(RestApiClient restApiClient,
                                    S3StorageService s3StorageService,
                                    FileConverterService fileConverterService,
                                    OrderValidator orderValidator,
                                    OrderTransformer orderTransformer,
                                    SourceRestProperties sourceProperties,
                                    TargetS3Properties targetProperties) {
        this.restApiClient = restApiClient;
        this.s3StorageService = s3StorageService;
        this.fileConverterService = fileConverterService;
        this.orderValidator = orderValidator;
        this.orderTransformer = orderTransformer;
        this.sourceProperties = sourceProperties;
        this.targetProperties = targetProperties;
    }

    public PipelineResult execute() {
        PipelineResult result = new PipelineResult();
        result.setSourceLocation(sourceProperties.getMethod() + " " + sourceProperties.getUrl());

        List<OrderRecord> sourceOrders = fetchOrders();
        result.setRecordsRead(sourceOrders.size());

        List<OrderRecord> transformed = new ArrayList<>();
        for (OrderRecord order : sourceOrders) {
            ValidationResult validation = orderValidator.validate(order);
            if (!validation.isValid()) {
                result.setRecordsInvalid(result.getRecordsInvalid() + 1);
                String orderRef = order.getOrderId() == null ? "<unknown>" : order.getOrderId();
                validation.getErrors().forEach(err -> result.addError("order " + orderRef + ": " + err));
                continue;
            }
            result.setRecordsValid(result.getRecordsValid() + 1);
            transformed.add(orderTransformer.transform(order));
        }

        if (!transformed.isEmpty()) {
            uploadToS3(transformed, result);
        }

        result.finish();
        log.info("rest-to-s3 pipeline finished: read={}, valid={}, invalid={}, delivered={}, status={}",
                result.getRecordsRead(), result.getRecordsValid(), result.getRecordsInvalid(),
                result.getRecordsDelivered(), result.getStatus());
        return result;
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

    private void uploadToS3(List<OrderRecord> transformed, PipelineResult result) {
        List<Map<String, Object>> rows = transformed.stream().map(OrderMapper::toRow).toList();
        byte[] content = fileConverterService.convert(rows, targetProperties.getFileFormat(), OrderMapper.CSV_COLUMNS);

        String key = targetProperties.getKeyPrefix()
                + "orders-" + Instant.now().toEpochMilli() + "." + targetProperties.getFileFormat().fileExtension();

        s3StorageService.upload(targetProperties.getBucket(), key, content, targetProperties.getFileFormat().contentType());

        result.setRecordsDelivered(transformed.size());
        result.setTargetLocation("s3://" + targetProperties.getBucket() + "/" + key);
        log.debug("Uploaded {} bytes for {} order(s) to {}", content.length, transformed.size(), result.getTargetLocation());
    }
}
