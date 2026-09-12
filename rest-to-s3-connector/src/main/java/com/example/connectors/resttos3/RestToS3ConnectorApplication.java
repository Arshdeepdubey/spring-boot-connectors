package com.example.connectors.resttos3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * rest-to-s3-connector: source (REST API) -&gt; validate -&gt; transform -&gt; convert to file -&gt; target (S3).
 * {@code scanBasePackages} widens component scanning to {@code com.example.connectors} so the
 * shared beans in the {@code common} module (REST client, S3 client, exception handler, ...)
 * are picked up alongside this connector's own beans.
 */
@SpringBootApplication(scanBasePackages = "com.example.connectors")
public class RestToS3ConnectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestToS3ConnectorApplication.class, args);
    }
}
