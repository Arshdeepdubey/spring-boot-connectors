package com.example.connectors.s3torest.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({SourceS3Properties.class, TargetRestProperties.class})
public class ConnectorPropertiesConfig {
}
