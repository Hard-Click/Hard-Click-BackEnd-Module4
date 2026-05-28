package com.wanted.backend.domain.identity.domain.repository;

import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.EmailVerification;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository {
    void save(EmailVerification verification);

    // 가장 최근 발송된 인증 정보 조회
    Optional<EmailVerification> findLatestByEmailAndPurpose(String email, EmailPurpose purpose);
    long countByEmailAndPurposeAndCreatedAtAfter(String email, EmailPurpose purpose, LocalDateTime after);
    Optional<EmailVerification> findByVerificationTokenAndPurpose(String token, EmailPurpose purpose);
}