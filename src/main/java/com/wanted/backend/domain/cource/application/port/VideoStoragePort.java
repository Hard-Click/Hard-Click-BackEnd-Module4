package com.wanted.backend.domain.cource.application.port;

public interface VideoStoragePort {
    PresignedUpload generatePresignedPutUrl(Long lessonId, String originalFilename);

    // 업로드 완료 전 실패했을 때 orphan 객체 정리
    void delete(String s3Key);

    // confirm 시 실제로 업로드됐는지, 크기가 허용치 이내인지 검증하기 위해 조회한다.
    // 객체가 존재하지 않으면 BusinessException(VIDEO_UPLOAD_NOT_FOUND)을 던진다.
    long getObjectSize(String s3Key);

    record PresignedUpload(String presignedUrl, String s3Key, String contentType) {}
}
