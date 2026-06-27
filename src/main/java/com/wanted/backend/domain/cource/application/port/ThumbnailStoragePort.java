package com.wanted.backend.domain.cource.application.port;

public interface ThumbnailStoragePort {
    StoredThumbnail store(Long courseId, String originalFilename, byte[] data);
    void delete(String key);

    record StoredThumbnail(String key, String presignedUrl) {}
}
