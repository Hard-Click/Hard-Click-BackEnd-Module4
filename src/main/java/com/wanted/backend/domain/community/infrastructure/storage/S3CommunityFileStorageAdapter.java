package com.wanted.backend.domain.community.infrastructure.storage;

import com.wanted.backend.domain.community.application.port.CommunityFileStoragePort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class S3CommunityFileStorageAdapter implements CommunityFileStoragePort {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png");
    private static final Duration PRESIGNED_URL_EXPIRY = Duration.ofDays(7);

    @Value("${aws.s3.bucket}")
    private String bucket;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public S3CommunityFileStorageAdapter(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @Override
    public String store(MultipartFile file, String prefix, long maxFileSize) {
        validate(file, maxFileSize);

        String extension = getExtension(file.getOriginalFilename());
        String key = prefix + "/" + UUID.randomUUID() + "." + extension;

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

        return s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                .signatureDuration(PRESIGNED_URL_EXPIRY)
                .getObjectRequest(r -> r.bucket(bucket).key(key))
                .build())
                .url()
                .toString();
    }

    @Override
    public void delete(String fileUrl) {
        try {
            String path = URI.create(fileUrl).getPath();
            String key = path.startsWith("/") ? path.substring(1) : path;
            s3Client.deleteObject(r -> r.bucket(bucket).key(key));
        } catch (Exception e) {
            log.warn("S3 파일 삭제 실패: {}", fileUrl, e);
        }
    }

    private void validate(MultipartFile file, long maxFileSize) {
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
