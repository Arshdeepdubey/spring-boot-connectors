package com.example.connectors.resttos3.controller;

import com.example.connectors.common.pipeline.PipelineResult;
import com.example.connectors.resttos3.service.RestToS3PipelineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RestToS3Controller.class)
class RestToS3ControllerTest {

    /**
     * Test configuration that provides a test implementation of RestToS3PipelineService.
     * Used to avoid Mockito ByteBuddy instrumentation issues on Java 25.
     */
    @TestConfiguration
    static class TestConfig {
        // Simple test implementation that returns a configured result
        static class TestRestToS3PipelineService extends RestToS3PipelineService {
            private PipelineResult resultToReturn;

            TestRestToS3PipelineService() {
                // Call parent constructor with null values - not actually used for testing
                super(null, null, null, null, null, null, null);
            }

            @Override
            public PipelineResult execute() {
                return resultToReturn;
            }

            void setResultToReturn(PipelineResult result) {
                this.resultToReturn = result;
            }
        }

        private static final TestRestToS3PipelineService testService = new TestRestToS3PipelineService();

        @Bean
        @Primary
        RestToS3PipelineService pipelineService() {
            return testService;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired(required = false)
    private RestToS3PipelineService pipelineService;

    @Test
    void executeReturnsPipelineSummary() throws Exception {
        PipelineResult result = new PipelineResult();
        result.setRecordsRead(3);
        result.setRecordsValid(3);
        result.setRecordsDelivered(3);
        result.setTargetLocation("s3://bucket/orders/orders-1.csv");
        result.finish();

        // Set the result on our test service
        if (pipelineService instanceof TestConfig.TestRestToS3PipelineService) {
            ((TestConfig.TestRestToS3PipelineService) pipelineService).setResultToReturn(result);
        }

        mockMvc.perform(post("/api/v1/connectors/rest-to-s3/execute"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.recordsRead").value(3))
                .andExpect(jsonPath("$.data.targetLocation").value("s3://bucket/orders/orders-1.csv"));
    }

    @Test
    void healthEndpointReportsUp() throws Exception {
        mockMvc.perform(get("/api/v1/connectors/rest-to-s3/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("rest-to-s3-connector is up"));
    }
}
