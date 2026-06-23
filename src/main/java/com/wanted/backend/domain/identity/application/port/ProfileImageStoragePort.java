package com.wanted.backend.domain.identity.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface ProfileImageStoragePort {

    String upload(MultipartFile file);

    String generatePresignedUrl(String s3Key);
}