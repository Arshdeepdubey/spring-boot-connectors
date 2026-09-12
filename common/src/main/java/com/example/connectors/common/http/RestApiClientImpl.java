package com.example.connectors.common.http;

import com.example.connectors.common.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactorySettings;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Map;

/**
 * Default {@link RestApiClient}: one Spring {@link RestClient} shared across all
 * calls, HTTP-method-agnostic, with configurable timeouts, header/auth injection
 * and a small fixed-backoff retry loop for transient failures. Used by every
 * connector to reach whatever source or target REST API it is configured with.
 */
@Component
@EnableConfigurationProperties(HttpClientProperties.class)
public class RestApiClientImpl implements RestApiClient {

    private static final Logger log = LoggerFactory.getLogger(RestApiClientImpl.class);

    private final RestClient restClient;
    private final HttpClientProperties properties;

    public RestApiClientImpl(HttpClientProperties properties) {
        this.properties = properties;
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .withReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(settings);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    RestApiClientImpl(HttpClientProperties properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    @Override
    public <T> T exchange(RestCallRequest request, Class<T> responseType) {
        int attempt = 0;
        RuntimeException lastError = null;
        while (attempt <= properties.getMaxRetries()) {
            attempt++;
            try {
                log.info("Calling {} {} (attempt {}/{})", request.getMethod(), request.getUrl(), attempt,
                        properties.getMaxRetries() + 1);
                return doExchange(request, responseType);
            } catch (RestClientException ex) {
                lastError = ex;
                log.warn("Call to {} {} failed on attempt {}: {}", request.getMethod(), request.getUrl(), attempt,
                        ex.getMessage());
                if (attempt > properties.getMaxRetries()) {
                    break;
                }
                sleep(properties.getRetryBackoffMs());
            }
        }
        throw new ExternalServiceException(
                "Call to " + request.getMethod() + " " + request.getUrl() + " failed after " + attempt + " attempt(s)",
                lastError);
    }

    private <T> T doExchange(RestCallRequest request, Class<T> responseType) {
        RestClient.RequestBodySpec spec = restClient
                .method(request.getMethod())
                .uri(request.getUrl());

        applyAuth(spec, request.getAuth());

        if (request.getHeaders() != null) {
            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                spec.header(header.getKey(), header.getValue());
            }
        }

        if (request.getBody() != null) {
            spec.body(request.getBody());
        }

        return spec.retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new ExternalServiceException(
                            "Upstream returned " + res.getStatusCode() + " for " + request.getMethod() + " " + request.getUrl());
                })
                .body(responseType);
    }

    private void applyAuth(RestClient.RequestBodySpec spec, AuthConfig auth) {
        if (auth == null) {
            return;
        }
        switch (auth.getType()) {
            case BASIC -> spec.headers(h -> h.setBasicAuth(auth.getUsername(), auth.getPassword()));
            case BEARER -> spec.headers(h -> h.setBearerAuth(auth.getBearerToken()));
            case API_KEY -> spec.header(auth.getApiKeyHeader(), auth.getApiKeyValue());
            case NONE -> { /* nothing to add */ }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
