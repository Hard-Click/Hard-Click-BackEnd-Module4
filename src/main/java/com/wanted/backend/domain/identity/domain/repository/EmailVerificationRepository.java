package com.wanted.backend.domain.identity.domain.repository;

import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.EmailVerification;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository {
    void save(EmailVerification verification);

    Optional<EmailVerification> findLatestByEmailAndPurpose(String email, EmailPurpose purpose);

    Optional<EmailVerification> findLatestPendingByEmailAndPurpose(String email, EmailPurpose purpose);

    Optional<EmailVerification> findByVerificationTokenAndPurpose(String token, EmailPurpose purpose);

    Optional<EmailVerification> findValidToken(String email, String token, EmailPurpose purpose, LocalDateTime now);

    long countByEmailAndPurposeAndCreatedAtAfter(String email, EmailPurpose purpose, LocalDateTime after);

    void revokeActiveByEmailAndPurpose(String email, EmailPurpose purpose);
}