package com.example.connectors.s3torest.controller;

import com.example.connectors.common.pipeline.PipelineResult;
import com.example.connectors.s3torest.service.S3ToRestPipelineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = S3ToRestController.class)
class S3ToRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private S3ToRestPipelineService pipelineService;

    @Test
    void executeWithExplicitKeyPassesItThrough() throws Exception {
        PipelineResult result = new PipelineResult();
        result.setRecordsRead(2);
        result.setRecordsDelivered(2);
        result.finish();
        when(pipelineService.execute(eq("orders/orders-42.csv"))).thenReturn(result);

        mockMvc.perform(post("/api/v1/connectors/s3-to-rest/execute").param("key", "orders/orders-42.csv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recordsDelivered").value(2));

        verify(pipelineService).execute("orders/orders-42.csv");
    }

    @Test
    void executeWithoutKeyFallsBackToConfiguredResolution() throws Exception {
        PipelineResult result = new PipelineResult();
        result.finish();
        when(pipelineService.execute(isNull())).thenReturn(result);

        mockMvc.perform(post("/api/v1/connectors/s3-to-rest/execute"))
                .andExpect(status().isOk());
    }
}
