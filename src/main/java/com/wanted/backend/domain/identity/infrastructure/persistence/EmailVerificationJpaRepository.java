package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerificationJpaEntity, Long> {

    Optional<EmailVerificationJpaEntity> findFirstByEmailAndPurposeOrderByCreatedAtDesc(String email, EmailPurpose purpose);
    long countByEmailAndPurposeAndCreatedAtAfter(String email, EmailPurpose purpose, LocalDateTime startOfDay);
    Optional<EmailVerificationJpaEntity> findByVerificationTokenAndPurpose(String verificationToken, EmailPurpose purpose);
}