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

    @Value("${aws.s3.region}")
    private String region;

    private final S3Presigner s3Presigner;

    public S3UrlPresigner(S3Presigner s3Presigner) {
        this.s3Presigner = s3Presigner;
    }

    /**
     * key를 만료 없는 공개 S3 URL로 변환한다. 버킷이 해당 prefix에 대해 public read로
     * 열려 있어야 한다(썸네일/프로필/커뮤니티 이미지처럼 접근 제어가 필요 없는 비민감 자산용).
     * 영상처럼 수강권 검증이 필요한 자산에는 사용하지 않는다 — presign()을 그대로 쓴다.
     */
    public String publicUrl(String key) {
        if (key == null || key.isBlank()) {
            return key;
        }
        if (key.startsWith("http://") || key.startsWith("https://")) {
            return key;
        }
        return "https://%s.s3.%s.amazonaws.com/%s".formatted(bucket, region, key);
    }

    /**
     * key를 조회용 Presigned URL로 변환한다.
     * - null/blank → 그대로 반환
     * - 이미 http(s) URL(과거 데이터/외부 URL) → 그대로 반환 (하위 호환)
     * - 그 외(S3 key) → 새 Presigned GET URL 발급
     */
    public String presign(String key) {
        return presign(key, PRESIGNED_URL_EXPIRY);
    }

    public String presign(String key, Duration expiration) {
        if (key == null || key.isBlank()) {
            return key;
        }
        if (key.startsWith("http://") || key.startsWith("https://")) {
            return key;
        }
        return s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(expiration)
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
