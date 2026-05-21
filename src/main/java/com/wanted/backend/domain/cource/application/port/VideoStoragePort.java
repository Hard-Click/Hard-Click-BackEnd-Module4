package com.wanted.backend.domain.cource.application.port;

public interface VideoStoragePort {
    String store(Long lessonId, String originalFilename, byte[] data);
}
