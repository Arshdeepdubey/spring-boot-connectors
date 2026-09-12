package com.example.connectors.resttodb.controller;

import com.example.connectors.common.exception.ResourceNotFoundException;
import com.example.connectors.common.pipeline.PipelineResult;
import com.example.connectors.resttodb.dto.OrderResponse;
import com.example.connectors.resttodb.service.OrderQueryService;
import com.example.connectors.resttodb.service.RestToDbPipelineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RestToDbController.class)
class RestToDbControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestToDbPipelineService pipelineService;

    @MockBean
    private OrderQueryService orderQueryService;

    @Test
    void executeReturnsPipelineSummary() throws Exception {
        PipelineResult result = new PipelineResult();
        result.setRecordsRead(1);
        result.setRecordsDelivered(1);
        result.finish();
        when(pipelineService.execute()).thenReturn(result);

        mockMvc.perform(post("/api/v1/connectors/rest-to-db/execute"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recordsDelivered").value(1));
    }

    @Test
    void getOrderReturns404WhenMissing() throws Exception {
        when(orderQueryService.findByOrderId("MISSING"))
                .thenThrow(new ResourceNotFoundException("No order found with orderId MISSING"));

        mockMvc.perform(get("/api/v1/connectors/rest-to-db/orders/MISSING"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void listOrdersReturnsPage() throws Exception {
        Page<OrderResponse> page = new PageImpl<>(java.util.List.of());
        when(orderQueryService.findAll(org.mockito.ArgumentMatchers.any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/connectors/rest-to-db/orders"))
                .andExpect(status().isOk());
    }
}
