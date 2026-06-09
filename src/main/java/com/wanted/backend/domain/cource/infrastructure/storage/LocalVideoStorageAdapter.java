package com.wanted.backend.domain.cource.infrastructure.storage;

import com.wanted.backend.domain.cource.application.port.VideoStoragePort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Component
public class LocalVideoStorageAdapter implements VideoStoragePort {

    // 허용 확장자 whitelist
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("mp4", "mov", "webm", "m4v");

    @Value("${app.video.upload-dir:uploads/videos}")
    private String uploadDir;

    @Value("${app.video.base-url:http://localhost:8080/uploads/videos/}")
    private String baseUrl;

    @Override
    public String store(Long lessonId, String originalFilename, byte[] data) {
        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            // 원본 파일명을 그대로 쓰지 않고 UUID 기반으로 생성 (경로 조작·중복·덮어쓰기 방지)
            String ext = extractAllowedExtension(originalFilename);
            String filename = lessonId + "_" + UUID.randomUUID() + "." + ext;

            Path target = dir.resolve(filename).normalize();
            // 업로드 디렉토리 밖으로 벗어나지 못하게 검증
            if (!target.startsWith(dir.normalize())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            Files.write(target, data);

            return baseUrl + filename;
        } catch (IOException e) {
            throw new RuntimeException("영상 저장에 실패했습니다.", e);
        }
    }

    private String extractAllowedExtension(String originalFilename) {
        if (originalFilename == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        String ext = originalFilename.substring(dot + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return ext;
    }
}
