package com.example.connectors.s3torest.service;

import com.example.connectors.common.exception.ExternalServiceException;
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
import com.example.connectors.s3torest.config.SourceS3Properties;
import com.example.connectors.s3torest.config.TargetRestProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates the full s3-to-rest pipeline: locate and download the orders file from
 * S3, parse it, validate and transform each record, then deliver every valid order to
 * the configured target REST API one at a time. A delivery failure for one order is
 * recorded and skipped rather than aborting the rest of the batch.
 */
@Service
public class S3ToRestPipelineService {

    private static final Logger log = LoggerFactory.getLogger(S3ToRestPipelineService.class);

    private final S3StorageService s3StorageService;
    private final RestApiClient restApiClient;
    private final FileConverterService fileConverterService;
    private final OrderValidator orderValidator;
    private final OrderTransformer orderTransformer;
    private final SourceS3Properties sourceProperties;
    private final TargetRestProperties targetProperties;

    public S3ToRestPipelineService(S3StorageService s3StorageService,
                                    RestApiClient restApiClient,
                                    FileConverterService fileConverterService,
                                    OrderValidator orderValidator,
                                    OrderTransformer orderTransformer,
                                    SourceS3Properties sourceProperties,
                                    TargetRestProperties targetProperties) {
        this.s3StorageService = s3StorageService;
        this.restApiClient = restApiClient;
        this.fileConverterService = fileConverterService;
        this.orderValidator = orderValidator;
        this.orderTransformer = orderTransformer;
        this.sourceProperties = sourceProperties;
        this.targetProperties = targetProperties;
    }

    public PipelineResult execute() {
        return execute(null);
    }

    /**
     * @param overrideKey when provided, process this exact S3 key instead of resolving
     *                    one from {@code connector.source.s3.key} / {@code key-prefix}
     */
    public PipelineResult execute(String overrideKey) {
        PipelineResult result = new PipelineResult();

        String key = resolveKey(overrideKey);
        result.setSourceLocation("s3://" + sourceProperties.getBucket() + "/" + key);

        byte[] content = s3StorageService.download(sourceProperties.getBucket(), key);
        List<Map<String, Object>> rows = fileConverterService.parse(content, sourceProperties.getFileFormat());
        result.setRecordsRead(rows.size());

        int delivered = 0;
        for (Map<String, Object> row : rows) {
            OrderRecord order = OrderMapper.fromRow(row);
            ValidationResult validation = orderValidator.validate(order);
            if (!validation.isValid()) {
                result.setRecordsInvalid(result.getRecordsInvalid() + 1);
                String orderRef = order.getOrderId() == null ? "<unknown>" : order.getOrderId();
                validation.getErrors().forEach(err -> result.addError("order " + orderRef + ": " + err));
                continue;
            }
            result.setRecordsValid(result.getRecordsValid() + 1);

            OrderRecord transformed = orderTransformer.transform(order);
            if (deliver(transformed, result)) {
                delivered++;
            }
        }

        result.setRecordsDelivered(delivered);
        result.setTargetLocation(targetProperties.getMethod() + " " + targetProperties.getUrl());
        result.finish();
        log.info("s3-to-rest pipeline finished: read={}, valid={}, invalid={}, delivered={}, failed={}, status={}",
                result.getRecordsRead(), result.getRecordsValid(), result.getRecordsInvalid(),
                result.getRecordsDelivered(), result.getRecordsFailed(), result.getStatus());
        return result;
    }

    private boolean deliver(OrderRecord order, PipelineResult result) {
        try {
            RestCallRequest request = RestCallRequest.builder()
                    .url(targetProperties.getUrl())
                    .method(targetProperties.getMethod())
                    .headers(targetProperties.getHeaders())
                    .auth(targetProperties.getAuth())
                    .body(order)
                    .build();
            restApiClient.exchange(request, String.class);
            return true;
        } catch (ExternalServiceException ex) {
            result.incrementFailed();
            result.addError("order " + order.getOrderId() + ": delivery failed - " + ex.getMessage());
            log.warn("Failed to deliver order {} to target API: {}", order.getOrderId(), ex.getMessage());
            return false;
        }
    }

    private String resolveKey(String overrideKey) {
        if (overrideKey != null && !overrideKey.isBlank()) {
            return overrideKey;
        }
        if (sourceProperties.getKey() != null && !sourceProperties.getKey().isBlank()) {
            return sourceProperties.getKey();
        }
        List<String> keys = s3StorageService.listObjectKeys(sourceProperties.getBucket(), sourceProperties.getKeyPrefix());
        return keys.stream()
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new ExternalServiceException(
                        "No objects found under s3://" + sourceProperties.getBucket() + "/" + sourceProperties.getKeyPrefix()));
    }
}
