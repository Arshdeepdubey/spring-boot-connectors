package com.example.connectors.resttos3;

import com.example.connectors.common.http.HttpClientProperties;
import com.example.connectors.common.pipeline.PipelineResult;
import com.example.connectors.common.pipeline.PipelineStatus;
import com.example.connectors.common.s3.S3StorageService;
import com.example.connectors.resttos3.service.RestToS3PipelineService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the full wiring (real HTTP client -&gt; validate -&gt; transform -&gt; convert)
 * against a WireMock stand-in for the source API. S3 itself is mocked out so this
 * test has no dependency on AWS or LocalStack.
 */
@SpringBootTest
@Import(IntegrationTestConfiguration.class)
class RestToS3IntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private RestToS3PipelineService pipelineService;

    @Autowired
    private S3StorageService s3StorageService;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("connector.http.connect-timeout-ms", () -> "5000");
        registry.add("connector.http.read-timeout-ms", () -> "10000");
        registry.add("connector.http.max-retries", () -> "2");
        registry.add("connector.http.retry-backoff-ms", () -> "500");
    }

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
        wireMockServer.stubFor(get(urlEqualTo("/api/orders"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {"orderId":"ORD-1","customerId":"CUST-1","productName":"Widget","quantity":2,"price":9.99,"orderDate":"2026-01-15","status":"NEW"},
                                  {"orderId":"ORD-2","customerId":"CUST-2","productName":"Gadget","quantity":1,"price":19.5,"orderDate":"2026-01-16"}
                                ]
                                """)));
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @Test
    void fetchesValidatesTransformsAndUploads() {
        PipelineResult result = pipelineService.execute();

        assertThat(result.getRecordsRead()).isEqualTo(2);
        assertThat(result.getRecordsValid()).isEqualTo(2);
        assertThat(result.getRecordsDelivered()).isEqualTo(2);
        assertThat(result.getStatus()).isEqualTo(PipelineStatus.SUCCESS);
    }
}
