package com.example.connectors.resttos3;

import com.example.connectors.common.http.HttpClientProperties;
import com.example.connectors.common.http.RestApiClient;
import com.example.connectors.common.http.RestApiClientImpl;
import com.example.connectors.common.s3.S3StorageService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * Test configuration that provides mock beans for testing.
 * Used to avoid Mockito ByteBuddy instrumentation issues on Java 25.
 */
@TestConfiguration
public class IntegrationTestConfiguration {

    public static class MockS3StorageService implements S3StorageService {
        public String lastUploadKey;
        public byte[] lastUploadContent;
        public String lastUploadContentType;

        @Override
        public String upload(String bucket, String key, byte[] content, String contentType) {
            this.lastUploadKey = key;
            this.lastUploadContent = content;
            this.lastUploadContentType = contentType;
            return key;
        }

        @Override
        public byte[] download(String bucket, String key) {
            return new byte[0];
        }

        @Override
        public List<String> listObjectKeys(String bucket, String prefix) {
            return List.of();
        }

        @Override
        public boolean exists(String bucket, String key) {
            return false;
        }
    }

    @Bean
    @Primary
    public S3StorageService s3StorageService() {
        return new MockS3StorageService();
    }

    @Bean
    @Primary
    public HttpClientProperties httpClientProperties() {
        HttpClientProperties props = new HttpClientProperties();
        props.setConnectTimeoutMs(5000);
        props.setReadTimeoutMs(10000);
        props.setMaxRetries(2);
        props.setRetryBackoffMs(500);
        return props;
    }

    @Bean
    @Primary
    public RestApiClient restApiClient(HttpClientProperties properties) {
        return new RestApiClientImpl(properties);
    }
}
