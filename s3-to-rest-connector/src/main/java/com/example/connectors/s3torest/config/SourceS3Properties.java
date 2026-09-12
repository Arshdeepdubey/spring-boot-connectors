package com.example.connectors.s3torest.config;

import com.example.connectors.common.fileconvert.FileFormat;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Where this connector reads its input file from. Either set {@link #key} for an exact
 * object, or leave it blank and set {@link #keyPrefix} to pick the most recent object
 * under that prefix (by key, which sorts by the embedded timestamp when files are
 * written by rest-to-s3-connector's naming convention).
 */
@ConfigurationProperties(prefix = "connector.source.s3")
public class SourceS3Properties {

    private String bucket;

    private String key;

    private String keyPrefix = "orders/";

    private FileFormat fileFormat = FileFormat.CSV;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public FileFormat getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(FileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }
}
