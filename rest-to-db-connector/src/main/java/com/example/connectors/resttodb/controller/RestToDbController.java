package com.example.connectors.resttodb.controller;

import com.example.connectors.common.pipeline.PipelineResult;
import com.example.connectors.common.web.ApiResponse;
import com.example.connectors.resttodb.dto.OrderResponse;
import com.example.connectors.resttodb.service.OrderQueryService;
import com.example.connectors.resttodb.service.RestToDbPipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST facade for the rest-to-db connector: {@code POST /execute} runs the pipeline,
 * the {@code GET} endpoints let callers read back what has been persisted so far.
 */
@RestController
@RequestMapping("/api/v1/connectors/rest-to-db")
public class RestToDbController {

    private static final Logger log = LoggerFactory.getLogger(RestToDbController.class);

    private final RestToDbPipelineService pipelineService;
    private final OrderQueryService orderQueryService;

    public RestToDbController(RestToDbPipelineService pipelineService, OrderQueryService orderQueryService) {
        this.pipelineService = pipelineService;
        this.orderQueryService = orderQueryService;
    }

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<PipelineResult>> execute() {
        log.info("Triggering rest-to-db pipeline run");
        PipelineResult result = pipelineService.execute();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> listOrders(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(orderQueryService.findAll(pageable)));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.ok(orderQueryService.findByOrderId(orderId)));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.ok("rest-to-db-connector is up"));
    }
}
