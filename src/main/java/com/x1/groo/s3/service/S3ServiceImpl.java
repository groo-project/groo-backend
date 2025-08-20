package com.x1.groo.s3.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.x1.groo.common.exception.CustomException;
import com.x1.groo.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.*;

@Service
public class S3ServiceImpl implements S3Service {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Autowired
    public S3ServiceImpl(AmazonS3Client amazonS3Client) {
        this.amazonS3Client = amazonS3Client;
    }

    @Override
    public String generatePresignedUrl(String fileName) {

        try {
            // Presigned URL 만료 시간 (5분)
            Date expiration = new Date();
            expiration.setTime(expiration.getTime() + (1000 * 60 * 5));

            // Presigned URL 생성 요청
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucketName, fileName)
                            .withMethod(HttpMethod.PUT)
                            .withExpiration(expiration);

            URL presignedUrl = amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest);
            return presignedUrl.toString();
        } catch (SdkClientException e) {
            throw new CustomException(ErrorCode.S3_PRESIGNED_URL_FAIL, e);
        }
    }

    @Override
    public List<String> getAllObjects(String prefix) {

        try {
            List<String> objectKeys = new ArrayList<>();

            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(bucketName)
                    .withPrefix(prefix);

            ListObjectsV2Result result;

            do {
                result = amazonS3Client.listObjectsV2(request);
                for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                    objectKeys.add(objectSummary.getKey());
                }
            } while (result.isTruncated());

            return objectKeys;
        } catch (SdkClientException e) {
            throw new CustomException(ErrorCode.S3_LIST_OBJECTS_FAIL, e);
        }
    }
}
