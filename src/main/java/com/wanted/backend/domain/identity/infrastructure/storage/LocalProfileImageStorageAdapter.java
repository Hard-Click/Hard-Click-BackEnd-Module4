package com.wanted.backend.domain.identity.infrastructure.storage;

import com.wanted.backend.domain.identity.application.port.ProfileImageStoragePort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Component
public class LocalProfileImageStorageAdapter implements ProfileImageStoragePort {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png");

    @Value("${identity.profile-image.upload-dir}")
    private String uploadDir;

    @Value("${identity.profile-image.url}")
    private String baseUrl;

    @Value("${identity.profile-image.max-size}")
    private long maxFileSize;

    @Override
    public String store(MultipartFile file) {
        // 저장 전에 프로필 이미지 전용 정책을 먼저 검증
        // 현재는 jpg/jpeg/png 확장자와 application.yaml의 최대 용량을 기준으로 판단
        validate(file);

        String extension = getExtension(file.getOriginalFilename());
        // 원본 파일명이 같아도 덮어쓰지 않도록 UUID 기반 저장명을 사용
        String savedFileName = UUID.randomUUID() + "." + extension;

        try {
            // 로컬 저장소 디렉터리가 없으면 생성한 뒤, UUID 파일명으로 실제 파일을 저장
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            Path target = dir.resolve(savedFileName);
            Files.copy(file.getInputStream(), target);

            // DB에는 로컬 경로가 아니라 클라이언트가 접근 가능한 URL을 저장
            return baseUrl + savedFileName;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private void validate(MultipartFile file) {
        // 프로필 이미지 정책은 application.yaml의 identity.profile-image.max-size 값을 따름
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        // 콘텐츠 타입은 클라이언트가 임의로 보낼 수 있으므로, 우선 파일명 확장자를 기준으로 제한
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private String getExtension(String filename) {
        // 확장자가 없는 파일은 허용 이미지 형식인지 판단할 수 없으므로 업로드를 거부
        if (filename == null || !filename.contains(".")) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
