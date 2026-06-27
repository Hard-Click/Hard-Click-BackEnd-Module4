package com.wanted.backend.domain.cource.application.port;

public interface VideoStoragePort {
    StoredVideo store(Long lessonId, String originalFilename, byte[] data);

    // store() 이후 DB 저장이 실패했을 때, 이미 업로드된 객체가 orphan으로 남지 않도록 정리한다.
    void delete(String key);

    record StoredVideo(String key, String presignedUrl) {
    }
}
