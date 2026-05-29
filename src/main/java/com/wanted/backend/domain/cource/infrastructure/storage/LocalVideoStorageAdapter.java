package com.wanted.backend.domain.cource.infrastructure.storage;

import com.wanted.backend.domain.cource.application.port.VideoStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class LocalVideoStorageAdapter implements VideoStoragePort {

    @Value("${app.video.upload-dir:uploads/videos}")
    private String uploadDir;

    @Value("${app.video.base-url:http://localhost:8080/uploads/videos/}")
    private String baseUrl;

    @Override
    public String store(Long lessonId, String originalFilename, byte[] data) {
        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            String filename = lessonId + "_" + originalFilename;
            Path target = dir.resolve(filename);
            Files.write(target, data);

            return baseUrl + filename;
        } catch (IOException e) {
            throw new RuntimeException("영상 저장에 실패했습니다.", e);
        }
    }
}
