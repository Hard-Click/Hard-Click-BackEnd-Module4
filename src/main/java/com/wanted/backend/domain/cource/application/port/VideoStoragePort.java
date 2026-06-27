package com.wanted.backend.domain.cource.application.port;

public interface VideoStoragePort {
    StoredVideo store(Long lessonId, String originalFilename, byte[] data);

    record StoredVideo(String key, String presignedUrl) {
    }
}
