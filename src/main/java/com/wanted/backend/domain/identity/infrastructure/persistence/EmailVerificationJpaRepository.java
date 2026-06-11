package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerificationJpaEntity, Long> {

    Optional<EmailVerificationJpaEntity> findFirstByEmailAndPurposeOrderByCreatedAtDesc(
            String email,
            EmailPurpose purpose
    );

    Optional<EmailVerificationJpaEntity> findFirstByEmailAndPurposeAndStatusOrderByCreatedAtDesc(
            String email,
            EmailPurpose purpose,
            VerificationStatus status
    );

    Optional<EmailVerificationJpaEntity> findByVerificationTokenAndPurpose(
            String verificationToken,
            EmailPurpose purpose
    );

    Optional<EmailVerificationJpaEntity> findFirstByEmailAndVerificationTokenAndPurposeAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
            String email,
            String verificationToken,
            EmailPurpose purpose,
            VerificationStatus status,
            LocalDateTime now
    );

    List<EmailVerificationJpaEntity> findByEmailAndPurposeAndStatusIn(
            String email,
            EmailPurpose purpose,
            Collection<VerificationStatus> statuses
    );

    long countByEmailAndPurposeAndCreatedAtAfter(
            String email,
            EmailPurpose purpose,
            LocalDateTime after
    );

    long deleteByExpiresAtBefore(LocalDateTime cutoff);
}
