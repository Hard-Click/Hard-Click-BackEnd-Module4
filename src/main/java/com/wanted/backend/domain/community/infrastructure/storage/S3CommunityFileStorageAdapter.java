package com.wanted.backend.domain.community.infrastructure.storage;

import com.wanted.backend.domain.community.application.port.CommunityFileStoragePort;
import com.wanted.backend.global.config.S3UrlPresigner;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class S3CommunityFileStorageAdapter implements CommunityFileStoragePort {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png");

    @Value("${aws.s3.bucket}")
    private String bucket;

    private final S3Client s3Client;
    private final S3UrlPresigner s3UrlPresigner;

    public S3CommunityFileStorageAdapter(S3Client s3Client, S3UrlPresigner s3UrlPresigner) {
        this.s3Client = s3Client;
        this.s3UrlPresigner = s3UrlPresigner;
    }

    /**
     * S3에 업로드하고 DB 저장용 key를 반환한다(만료되는 URL 저장 금지).
     */
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
        } catch (S3Exception e) {
            log.error("S3 upload failed: status={}, errorCode={}, message={}, requestId={}",
                    e.statusCode(),
                    e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "N/A",
                    e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : "N/A",
                    e.requestId(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, e);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, e);
        }

        return key;
    }

    /**
     * 저장된 key를 조회용 Presigned URL로 변환한다(응답 직전 호출).
     */
    @Override
    public String presignUrl(String key) {
        return s3UrlPresigner.presign(key);
    }

    @Override
    public void delete(String storedValue) {
        try {
            // 저장값이 key든(현재) 과거의 전체 URL이든 모두 삭제 가능하도록 key 추출
            String key = s3UrlPresigner.extractKey(storedValue);
            s3Client.deleteObject(r -> r.bucket(bucket).key(key));
        } catch (Exception e) {
            log.warn("S3 파일 삭제 실패: {}", storedValue, e);
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
