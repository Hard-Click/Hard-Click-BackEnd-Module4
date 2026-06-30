package com.wanted.backend.domain.cource.infrastructure.storage;

import com.wanted.backend.domain.cource.application.port.VideoStoragePort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class S3VideoStorageAdapter implements VideoStoragePort {

    private static final String KEY_PREFIX = "videos";
    private static final Duration PRESIGNED_PUT_EXPIRY = Duration.ofMinutes(15);

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("mp4", "mov", "webm", "m4v");
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "mp4", "video/mp4",
            "mov", "video/quicktime",
            "webm", "video/webm",
            "m4v", "video/x-m4v"
    );

    @Value("${aws.s3.bucket}")
    private String bucket;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public S3VideoStorageAdapter(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @Override
    public PresignedUpload generatePresignedPutUrl(Long lessonId, String originalFilename) {
        String ext = extractAllowedExtension(originalFilename);
        String s3Key = KEY_PREFIX + "/" + lessonId + "_" + UUID.randomUUID() + "." + ext;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .contentType(CONTENT_TYPES.getOrDefault(ext, "application/octet-stream"))
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGNED_PUT_EXPIRY)
                .putObjectRequest(putRequest)
                .build();

        String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
        return new PresignedUpload(presignedUrl, s3Key);
    }

    @Override
    public void delete(String s3Key) {
        try {
            s3Client.deleteObject(r -> r.bucket(bucket).key(s3Key));
        } catch (Exception e) {
            log.warn("S3 영상 삭제 실패: {}", s3Key, e);
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
