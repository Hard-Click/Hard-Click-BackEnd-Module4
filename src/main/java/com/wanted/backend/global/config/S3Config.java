package com.wanted.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.presign.access-key:}")
    private String presignAccessKey;

    @Value("${aws.s3.presign.secret-key:}")
    private String presignSecretKey;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(presignCredentialsProvider())
                .build();
    }

    /**
     * presign 서명은 항상 영구 키로 해야 한다 — EC2 인스턴스 역할의 임시 STS 자격증명으로 서명하면
     * SigV4 규칙상 X-Amz-Expires(7일)와 무관하게 session token 만료(~1시간) 시점에 URL이 즉시
     * 무효화된다. 전용 키가 설정되지 않은 환경(로컬 등)에서는 기존 DefaultCredentialsProvider로 폴백한다.
     */
    AwsCredentialsProvider presignCredentialsProvider() {
        if (presignAccessKey.isBlank() || presignSecretKey.isBlank()) {
            return DefaultCredentialsProvider.create();
        }
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(presignAccessKey, presignSecretKey)
        );
    }
}
