package com.wanted.backend.domain.community.infrastructure.file;

import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


public class FileUploadUtils {

    // 허용 확장자 목록 (jpg, jpeg, png만 허용)
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png");

    // 파일 저장 + 형식/크기 검증 + UUID 난수화 처리
    public static String saveFile(MultipartFile file, String uploadDir, long maxFileSize) throws IOException {

        // 파일 확장자 추출 및 허용 형식 검증
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }

        // 파일 크기 검증 (community BC 전용 제한값 적용)
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        // UUID로 파일명 난수화 (동일 파일명 중복 저장 방지)
        String savedFileName = UUID.randomUUID() + "." + extension;

        // 저장 디렉토리 없으면 자동 생성
        Path dirPath = Paths.get(uploadDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 실제 디스크에 파일 저장
        Path filePath = dirPath.resolve(savedFileName);
        Files.copy(file.getInputStream(), filePath);

        return savedFileName;
    }

    // 파일 삭제 (DB 저장 실패 시 롤백용)
    public static void deleteFile(String uploadDir, String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // 삭제 실패해도 주요 로직에 영향 주지 않음
        }
    }

    // 파일명에서 확장자 추출
    private static String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}