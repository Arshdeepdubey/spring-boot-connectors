package com.example.connectors.resttos3.controller;

import com.example.connectors.common.pipeline.PipelineResult;
import com.example.connectors.common.web.ApiResponse;
import com.example.connectors.resttos3.service.RestToS3PipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST facade for the rest-to-s3 connector. A run is triggered on demand via
 * {@code POST /execute}; wire this up to a scheduler, message trigger, or external
 * orchestrator as needed for production use.
 */
@RestController
@RequestMapping("/api/v1/connectors/rest-to-s3")
public class RestToS3Controller {

    private static final Logger log = LoggerFactory.getLogger(RestToS3Controller.class);

    private final RestToS3PipelineService pipelineService;

    public RestToS3Controller(RestToS3PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<PipelineResult>> execute() {
        log.info("Triggering rest-to-s3 pipeline run");
        PipelineResult result = pipelineService.execute();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.ok("rest-to-s3-connector is up"));
    }
}
