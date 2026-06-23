package com.wanted.backend.domain.identity.infrastructure.storage;

import com.wanted.backend.domain.identity.application.port.ProfileImageStoragePort;
import com.wanted.backend.global.config.S3UrlPresigner;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class S3ProfileImageStorageAdapter implements ProfileImageStoragePort {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png");

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${identity.profile-image.max-size}")
    private long maxFileSize;

    @Value("${aws.s3.presigned-url.profile-minutes}")
    private long presignedUrlExpiryMinutes;

    private final S3Client s3Client;
    private final S3UrlPresigner s3UrlPresigner;

    public S3ProfileImageStorageAdapter(S3Client s3Client, S3UrlPresigner s3UrlPresigner) {
        this.s3Client = s3Client;
        this.s3UrlPresigner = s3UrlPresigner;
    }

    /**
     * S3에 업로드하고 DB 저장용 key를 반환한다(만료되는 URL 저장 금지).
     */
    @Override
    public String upload(MultipartFile file) {
        validate(file);

        String extension = getExtension(file.getOriginalFilename());
        String key = "profiles/" + UUID.randomUUID() + "." + extension;

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, e);
        }

        return key;
    }

    @Override
    public String generatePresignedUrl(String s3Key) {
        return s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(presignedUrlExpiryMinutes))
                        .getObjectRequest(r -> r.bucket(bucket).key(s3Key))
                        .build())
                .url()
                .toString();
    }

    private void validate(MultipartFile file) {
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}