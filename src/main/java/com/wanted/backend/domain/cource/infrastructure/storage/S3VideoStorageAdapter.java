package com.wanted.backend.domain.cource.infrastructure.storage;

import com.wanted.backend.domain.cource.application.port.VideoStoragePort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 강의 영상을 AWS S3에 저장하고 재생용 Presigned URL을 반환한다.
 * 커뮤니티/프로필 이미지 어댑터와 동일한 패턴(S3Client put + Presigner get).
 * Presigned URL은 SigV4 최대치인 7일 만료를 사용한다.
 */
@Slf4j
@Component
public class S3VideoStorageAdapter implements VideoStoragePort {

    private static final String KEY_PREFIX = "videos";
    private static final Duration PRESIGNED_URL_EXPIRY = Duration.ofDays(7);

    // 허용 확장자 whitelist (기존 Local 어댑터와 동일)
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("mp4", "mov", "webm", "m4v");

    // 확장자 → Content-Type (브라우저 스트리밍을 위해 명시)
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
    public String store(Long lessonId, String originalFilename, byte[] data) {
        if (data == null || data.length == 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String ext = extractAllowedExtension(originalFilename);
        // 원본 파일명을 그대로 쓰지 않고 UUID 기반 키 생성 (경로 조작·중복·덮어쓰기 방지)
        String key = KEY_PREFIX + "/" + lessonId + "_" + UUID.randomUUID() + "." + ext;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(CONTENT_TYPES.getOrDefault(ext, "application/octet-stream"))
                        .contentLength((long) data.length)
                        .build(),
                RequestBody.fromBytes(data)
        );

        return s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(PRESIGNED_URL_EXPIRY)
                        .getObjectRequest(r -> r.bucket(bucket).key(key))
                        .build())
                .url()
                .toString();
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
