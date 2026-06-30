package com.wanted.backend.domain.cource.infrastructure.storage;

import com.wanted.backend.domain.cource.application.port.VideoStoragePort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3VideoStorageAdapter implements VideoStoragePort {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("mp4", "mov", "webm", "m4v");
    private static final int PRESIGNED_PUT_EXPIRY_MINUTES = 15;

    private final S3Presigner s3Presigner;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Override
    public PresignedUpload generatePresignedPutUrl(Long lessonId, String originalFilename) {
        String ext = extractAllowedExtension(originalFilename);
        String s3Key = "videos/" + lessonId + "_" + UUID.randomUUID() + "." + ext;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .contentType(resolveContentType(ext))
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(PRESIGNED_PUT_EXPIRY_MINUTES))
                .putObjectRequest(putRequest)
                .build();

        String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
        return new PresignedUpload(presignedUrl, s3Key);
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

    private String resolveContentType(String ext) {
        return switch (ext) {
            case "mp4", "m4v" -> "video/mp4";
            case "mov" -> "video/quicktime";
            case "webm" -> "video/webm";
            default -> "application/octet-stream";
        };
    }
}
