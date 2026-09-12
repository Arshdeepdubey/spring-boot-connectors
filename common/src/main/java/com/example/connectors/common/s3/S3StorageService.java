package com.example.connectors.common.s3;

import java.util.List;

/** Thin, connector-facing abstraction over the AWS S3 operations the pipelines need. */
public interface S3StorageService {

    /** Uploads bytes to {@code bucket/key} and returns the key it was stored under. */
    String upload(String bucket, String key, byte[] content, String contentType);

    /** Downloads and returns the full object content. */
    byte[] download(String bucket, String key);

    /** Lists object keys under a bucket/prefix, most recent last-modified last is not guaranteed by S3 itself. */
    List<String> listObjectKeys(String bucket, String prefix);

    boolean exists(String bucket, String key);
}
