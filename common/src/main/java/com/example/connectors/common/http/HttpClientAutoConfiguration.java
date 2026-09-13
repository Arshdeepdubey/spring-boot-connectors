package com.example.connectors.common.http;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for the REST client and HTTP client properties.
 * Provides {@link RestApiClientImpl} as the default {@link RestApiClient} implementation.
 */
@Configuration
@EnableConfigurationProperties(HttpClientProperties.class)
public class HttpClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestApiClient restApiClient(HttpClientProperties properties) {
        return new RestApiClientImpl(properties);
    }
}
