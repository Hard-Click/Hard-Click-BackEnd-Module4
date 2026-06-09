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
            // 절대 경로로 정규화하여 기준 디렉토리를 고정
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            // 원본 파일명을 그대로 쓰지 않고 UUID 기반으로 생성 (경로 조작·중복·덮어쓰기 방지)
            String ext = extractAllowedExtension(originalFilename);
            String filename = lessonId + "_" + UUID.randomUUID() + "." + ext;

            // 최종 경로도 절대 경로로 정규화 후 기준 디렉토리 안에 있는지 검증 (경로 탈출 방어)
            Path target = dir.resolve(filename).toAbsolutePath().normalize();
            if (!target.startsWith(dir)) {
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
