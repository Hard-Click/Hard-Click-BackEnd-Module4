package com.wanted.backend.domain.cource.infrastructure.storage;

import com.wanted.backend.domain.cource.application.port.ThumbnailStoragePort;
import com.wanted.backend.global.config.S3UrlPresigner;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class S3ThumbnailStorageAdapter implements ThumbnailStoragePort {

    private static final String KEY_PREFIX = "thumbnails";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "jpg",  "image/jpeg",
            "jpeg", "image/jpeg",
            "png",  "image/png"
    );

    @Value("${aws.s3.bucket}")
    private String bucket;

    private final S3Client s3Client;
    private final S3UrlPresigner s3UrlPresigner;

    public S3ThumbnailStorageAdapter(S3Client s3Client, S3UrlPresigner s3UrlPresigner) {
        this.s3Client = s3Client;
        this.s3UrlPresigner = s3UrlPresigner;
    }

    @Override
    public StoredThumbnail store(Long courseId, String originalFilename, byte[] data) {
        if (data == null || data.length == 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String ext = extractAllowedExtension(originalFilename);
        String key = KEY_PREFIX + "/" + courseId + "_" + UUID.randomUUID() + "." + ext;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(CONTENT_TYPES.get(ext))
                        .contentLength((long) data.length)
                        .build(),
                RequestBody.fromBytes(data)
        );

        return new StoredThumbnail(key, s3UrlPresigner.publicUrl(key));
    }

    @Override
    public void delete(String key) {
        try {
            s3Client.deleteObject(r -> r.bucket(bucket).key(key));
        } catch (Exception e) {
            log.warn("S3 썸네일 삭제 실패: {}", key, e);
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
