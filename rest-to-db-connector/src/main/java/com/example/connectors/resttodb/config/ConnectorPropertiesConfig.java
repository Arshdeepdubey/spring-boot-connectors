package com.example.connectors.resttodb.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SourceRestProperties.class)
public class ConnectorPropertiesConfig {
}
