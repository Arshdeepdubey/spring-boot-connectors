package com.example.connectors.common.s3;

import com.example.connectors.common.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3StorageServiceImpl implements S3StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageServiceImpl.class);

    private final S3Client s3Client;

    public S3StorageServiceImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String upload(String bucket, String key, byte[] content, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(content));
            log.info("Uploaded {} bytes to s3://{}/{}", content.length, bucket, key);
            return key;
        } catch (S3Exception ex) {
            throw new ExternalServiceException("Failed to upload object to s3://" + bucket + "/" + key, ex);
        }
    }

    @Override
    public byte[] download(String bucket, String key) {
        try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(
                GetObjectRequest.builder().bucket(bucket).key(key).build())) {
            return response.readAllBytes();
        } catch (NoSuchKeyException ex) {
            throw new ExternalServiceException("Object s3://" + bucket + "/" + key + " does not exist", ex);
        } catch (S3Exception | IOException ex) {
            throw new ExternalServiceException("Failed to download object from s3://" + bucket + "/" + key, ex);
        }
    }

    @Override
    public List<String> listObjectKeys(String bucket, String prefix) {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .build();
            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            return response.contents().stream().map(o -> o.key()).collect(Collectors.toList());
        } catch (S3Exception ex) {
            throw new ExternalServiceException("Failed to list objects in s3://" + bucket + "/" + prefix, ex);
        }
    }

    @Override
    public boolean exists(String bucket, String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
            return true;
        } catch (NoSuchKeyException ex) {
            return false;
        } catch (S3Exception ex) {
            throw new ExternalServiceException("Failed to check existence of s3://" + bucket + "/" + key, ex);
        }
    }
}
