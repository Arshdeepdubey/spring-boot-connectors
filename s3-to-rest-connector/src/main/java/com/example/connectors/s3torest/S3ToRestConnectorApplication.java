package com.example.connectors.s3torest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * s3-to-rest-connector: source (S3 file) -&gt; validate -&gt; transform -&gt; target (REST API).
 * {@code scanBasePackages} widens component scanning to {@code com.example.connectors} so the
 * shared beans in the {@code common} module are picked up alongside this connector's own beans.
 */
@SpringBootApplication(scanBasePackages = "com.example.connectors")
public class S3ToRestConnectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(S3ToRestConnectorApplication.class, args);
    }
}
