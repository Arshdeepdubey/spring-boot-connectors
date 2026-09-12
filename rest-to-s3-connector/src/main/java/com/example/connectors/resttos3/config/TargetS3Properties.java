package com.example.connectors.resttos3.config;

import com.example.connectors.common.fileconvert.FileFormat;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Where and in what format this connector writes the archived orders file in S3. */
@ConfigurationProperties(prefix = "connector.target.s3")
public class TargetS3Properties {

    private String bucket;

    /** Key prefix ("folder") the output file is written under. */
    private String keyPrefix = "orders/";

    private FileFormat fileFormat = FileFormat.CSV;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
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
