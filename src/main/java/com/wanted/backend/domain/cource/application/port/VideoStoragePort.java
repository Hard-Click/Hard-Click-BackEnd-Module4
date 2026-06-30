package com.wanted.backend.domain.cource.application.port;

public interface VideoStoragePort {
    PresignedUpload generatePresignedPutUrl(Long lessonId, String originalFilename);

    // 업로드 완료 전 실패했을 때 orphan 객체 정리
    void delete(String s3Key);

    record PresignedUpload(String presignedUrl, String s3Key) {}
}
