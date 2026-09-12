package com.example.connectors.resttos3.controller;

import com.example.connectors.common.pipeline.PipelineResult;
import com.example.connectors.resttos3.service.RestToS3PipelineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RestToS3Controller.class)
class RestToS3ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestToS3PipelineService pipelineService;

    @Test
    void executeReturnsPipelineSummary() throws Exception {
        PipelineResult result = new PipelineResult();
        result.setRecordsRead(3);
        result.setRecordsValid(3);
        result.setRecordsDelivered(3);
        result.setTargetLocation("s3://bucket/orders/orders-1.csv");
        result.finish();

        when(pipelineService.execute()).thenReturn(result);

        mockMvc.perform(post("/api/v1/connectors/rest-to-s3/execute"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.recordsRead").value(3))
                .andExpect(jsonPath("$.data.targetLocation").value("s3://bucket/orders/orders-1.csv"));

        verify(pipelineService).execute();
    }

    @Test
    void healthEndpointReportsUp() throws Exception {
        mockMvc.perform(get("/api/v1/connectors/rest-to-s3/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("rest-to-s3-connector is up"));
    }
}
