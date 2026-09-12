package com.example.connectors.common.http;

import com.example.connectors.common.exception.ExternalServiceException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestApiClientImplTest {

    private WireMockServer wireMockServer;
    private RestApiClient client;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        HttpClientProperties properties = new HttpClientProperties();
        properties.setMaxRetries(1);
        properties.setRetryBackoffMs(10);
        client = new RestApiClientImpl(properties);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    private String baseUrl() {
        return "http://localhost:" + wireMockServer.port();
    }

    @Test
    void getReturnsBody() {
        wireMockServer.stubFor(get(urlEqualTo("/things/1"))
                .willReturn(aResponse().withStatus(200).withBody("hello")));

        String body = client.exchange(RestCallRequest.builder().url(baseUrl() + "/things/1").method(HttpMethod.GET).build(),
                String.class);

        assertThat(body).isEqualTo("hello");
    }

    @Test
    void postSendsBodyAndSupportsPatchAndDelete() {
        wireMockServer.stubFor(post(urlEqualTo("/things")).willReturn(aResponse().withStatus(201).withBody("created")));
        wireMockServer.stubFor(patch(urlEqualTo("/things/1")).willReturn(aResponse().withStatus(200).withBody("patched")));
        wireMockServer.stubFor(delete(urlEqualTo("/things/1")).willReturn(aResponse().withStatus(204)));

        String created = client.exchange(RestCallRequest.builder()
                .url(baseUrl() + "/things").method(HttpMethod.POST).body("{\"name\":\"x\"}").build(), String.class);
        String patched = client.exchange(RestCallRequest.builder()
                .url(baseUrl() + "/things/1").method(HttpMethod.PATCH).body("{\"name\":\"y\"}").build(), String.class);
        client.exchange(RestCallRequest.builder().url(baseUrl() + "/things/1").method(HttpMethod.DELETE).build(), Void.class);

        assertThat(created).isEqualTo("created");
        assertThat(patched).isEqualTo("patched");
        verify(postRequestedFor(urlEqualTo("/things")));
        verify(deleteRequestedFor(urlEqualTo("/things/1")));
    }

    @Test
    void apiKeyAuthIsSentAsHeader() {
        wireMockServer.stubFor(get(urlEqualTo("/secure"))
                .withHeader("X-API-Key", equalTo("secret-123"))
                .willReturn(aResponse().withStatus(200).withBody("ok")));

        AuthConfig auth = new AuthConfig();
        auth.setType(AuthType.API_KEY);
        auth.setApiKeyHeader("X-API-Key");
        auth.setApiKeyValue("secret-123");

        String body = client.exchange(RestCallRequest.builder()
                .url(baseUrl() + "/secure").method(HttpMethod.GET).auth(auth).build(), String.class);

        assertThat(body).isEqualTo("ok");
    }

    @Test
    void serverErrorTriggersRetryThenThrowsExternalServiceException() {
        wireMockServer.stubFor(get(urlEqualTo("/flaky")).willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> client.exchange(
                RestCallRequest.builder().url(baseUrl() + "/flaky").method(HttpMethod.GET).build(), String.class))
                .isInstanceOf(ExternalServiceException.class);

        // maxRetries=1 -> 2 total attempts
        wireMockServer.verify(2, com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor(urlEqualTo("/flaky")));
    }
}
