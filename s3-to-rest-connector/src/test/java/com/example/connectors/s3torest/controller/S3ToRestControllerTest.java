package com.example.connectors.s3torest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.connectors.common.pipeline.PipelineResult;
import com.example.connectors.s3torest.service.S3ToRestPipelineService;

@WebMvcTest(controllers = S3ToRestController.class)
class S3ToRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestS3ToRestPipelineService pipelineService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public S3ToRestPipelineService pipelineService() {
            return new TestS3ToRestPipelineService();
        }
    }

    public static class TestS3ToRestPipelineService extends S3ToRestPipelineService {
        public PipelineResult resultToReturn;

        // Minimal constructor - parent needs services, but we override execute() anyway
        public TestS3ToRestPipelineService() {
            super(null, null, null, null, null, null, null);
        }

        @Override
        public PipelineResult execute() {
            return resultToReturn;
        }

        @Override
        public PipelineResult execute(String overrideKey) {
            return resultToReturn;
        }
    }

    @Test
    void executeWithExplicitKeyPassesItThrough() throws Exception {
        PipelineResult result = new PipelineResult();
        result.setRecordsRead(2);
        result.setRecordsDelivered(2);
        result.finish();
        pipelineService.resultToReturn = result;

        mockMvc.perform(post("/api/v1/connectors/s3-to-rest/execute").param("key", "orders/orders-42.csv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recordsDelivered").value(2));
    }

    @Test
    void executeWithoutKeyFallsBackToConfiguredResolution() throws Exception {
        PipelineResult result = new PipelineResult();
        result.finish();
        pipelineService.resultToReturn = result;

        mockMvc.perform(post("/api/v1/connectors/s3-to-rest/execute"))
                .andExpect(status().isOk());
    }
}
