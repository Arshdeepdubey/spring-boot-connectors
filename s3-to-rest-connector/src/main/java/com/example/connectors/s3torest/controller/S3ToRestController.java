package com.example.connectors.s3torest.controller;

import com.example.connectors.common.pipeline.PipelineResult;
import com.example.connectors.common.web.ApiResponse;
import com.example.connectors.s3torest.service.S3ToRestPipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST facade for the s3-to-rest connector. {@code key} lets a caller target a
 * specific S3 object; omit it to fall back to the configured key/prefix resolution.
 */
@RestController
@RequestMapping("/api/v1/connectors/s3-to-rest")
public class S3ToRestController {

    private static final Logger log = LoggerFactory.getLogger(S3ToRestController.class);

    private final S3ToRestPipelineService pipelineService;

    public S3ToRestController(S3ToRestPipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<PipelineResult>> execute(
            @RequestParam(required = false) String key) {
        log.info("Triggering s3-to-rest pipeline run (key={})", key);
        PipelineResult result = pipelineService.execute(key);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.ok("s3-to-rest-connector is up"));
    }
}
