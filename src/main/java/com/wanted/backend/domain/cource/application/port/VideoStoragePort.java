package com.wanted.backend.domain.cource.application.port;

public interface VideoStoragePort {
    PresignedUpload generatePresignedPutUrl(Long lessonId, String originalFilename);

    record PresignedUpload(String presignedUrl, String s3Key) {}
}
