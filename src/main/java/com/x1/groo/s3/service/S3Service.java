package com.x1.groo.s3.service;

import java.util.List;

public interface S3Service {
    String generatePresignedUrl(String fileName);

    List<String> getAllObjects(String prefix);
}
