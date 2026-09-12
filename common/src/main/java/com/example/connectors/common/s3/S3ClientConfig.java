package com.example.connectors.common.s3;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

/**
 * Builds the shared {@link S3Client} bean. Static keys are only used when explicitly
 * configured (typically local development); otherwise the SDK's default credential
 * provider chain applies, which is the safer choice for anything deployed to AWS
 * (IAM role on ECS/EKS/EC2, environment variables injected by the platform, etc.).
 */
@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3ClientConfig {

    @Bean
    public S3Client s3Client(S3Properties properties) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(credentialsProvider(properties))
                .forcePathStyle(properties.isPathStyleAccessEnabled());

        if (properties.getEndpointOverride() != null && !properties.getEndpointOverride().isBlank()) {
            builder.endpointOverride(URI.create(properties.getEndpointOverride()));
        }

        return builder.build();
    }

    private AwsCredentialsProvider credentialsProvider(S3Properties properties) {
        if (properties.getAccessKey() != null && !properties.getAccessKey().isBlank()
                && properties.getSecretKey() != null && !properties.getSecretKey().isBlank()) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey()));
        }
        return DefaultCredentialsProvider.create();
    }
}
