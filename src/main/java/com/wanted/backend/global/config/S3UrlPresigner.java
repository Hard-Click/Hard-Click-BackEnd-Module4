package com.wanted.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URI;
import java.time.Duration;

/**
 * S3 객체 key로부터 조회용 Presigned URL을 생성하는 공용 컴포넌트.
 * <p>
 * DB에는 만료되는 URL이 아니라 S3 key만 저장하고, 응답을 만들 때마다 이 컴포넌트로
 * 새 Presigned URL을 발급한다(만료 영구 해결). 커뮤니티/프로필/영상 어댑터가 공유한다.
 */
@Component
public class S3UrlPresigner {

    private static final Duration PRESIGNED_URL_EXPIRY = Duration.ofDays(7);

    @Value("${aws.s3.bucket}")
    private String bucket;

    private final S3Presigner s3Presigner;

    public S3UrlPresigner(S3Presigner s3Presigner) {
        this.s3Presigner = s3Presigner;
    }

    /**
     * key를 조회용 Presigned URL로 변환한다.
     * - null/blank → 그대로 반환
     * - 이미 http(s) URL(과거 데이터/외부 URL) → 그대로 반환 (하위 호환)
     * - 그 외(S3 key) → 새 Presigned GET URL 발급
     */
    public String presign(String key) {
        if (key == null || key.isBlank()) {
            return key;
        }
        if (key.startsWith("http://") || key.startsWith("https://")) {
            return key;
        }
        return s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(PRESIGNED_URL_EXPIRY)
                        .getObjectRequest(r -> r.bucket(bucket).key(key))
                        .build())
                .url()
                .toString();
    }

    /**
     * 저장된 값(S3 key 또는 과거의 전체 URL)에서 삭제용 S3 key를 추출한다.
     */
    public String extractKey(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return storedValue;
        }
        if (storedValue.startsWith("http://") || storedValue.startsWith("https://")) {
            String path = URI.create(storedValue).getPath();
            return path.startsWith("/") ? path.substring(1) : path;
        }
        return storedValue;
    }
}
