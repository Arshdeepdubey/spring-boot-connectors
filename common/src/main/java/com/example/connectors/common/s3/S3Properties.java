package com.example.connectors.common.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AWS S3 connectivity settings, shared by both S3-facing connectors.
 * Credentials are optional: when {@link #accessKey}/{@link #secretKey} are left blank,
 * the AWS SDK default credential provider chain is used instead (environment variables,
 * shared config/credentials file, container/instance profile credentials, etc.) which is
 * the recommended approach outside of local development.
 */
@ConfigurationProperties(prefix = "connector.aws.s3")
public class S3Properties {

    /** AWS region, e.g. us-east-1. */
    private String region = "us-east-1";

    /** Optional static access key. Prefer the default credential chain in real environments. */
    private String accessKey;

    /** Optional static secret key. Prefer the default credential chain in real environments. */
    private String secretKey;

    /** Optional custom endpoint, used to point the client at LocalStack for local development/testing. */
    private String endpointOverride;

    /** Required for path-style access, which LocalStack and most S3-compatible stores need. */
    private boolean pathStyleAccessEnabled = false;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getEndpointOverride() {
        return endpointOverride;
    }

    public void setEndpointOverride(String endpointOverride) {
        this.endpointOverride = endpointOverride;
    }

    public boolean isPathStyleAccessEnabled() {
        return pathStyleAccessEnabled;
    }

    public void setPathStyleAccessEnabled(boolean pathStyleAccessEnabled) {
        this.pathStyleAccessEnabled = pathStyleAccessEnabled;
    }
}
