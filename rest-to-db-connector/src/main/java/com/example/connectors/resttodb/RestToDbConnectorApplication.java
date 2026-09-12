package com.example.connectors.resttodb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * rest-to-db-connector: source (REST API) -&gt; validate -&gt; transform -&gt; target (Postgres).
 * {@code scanBasePackages} widens component scanning to {@code com.example.connectors} so the
 * shared beans in the {@code common} module are picked up alongside this connector's own beans.
 */
@SpringBootApplication(scanBasePackages = "com.example.connectors")
public class RestToDbConnectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestToDbConnectorApplication.class, args);
    }
}
