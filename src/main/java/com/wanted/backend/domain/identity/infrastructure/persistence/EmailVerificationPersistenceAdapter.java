package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.EmailVerification;
import com.wanted.backend.domain.identity.domain.model.VerificationStatus;
import com.wanted.backend.domain.identity.domain.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailVerificationPersistenceAdapter implements EmailVerificationRepository {

    private final EmailVerificationJpaRepository jpaRepository;

    @Override
    public void save(EmailVerification domain) {
        EmailVerificationJpaEntity entity;

        if (domain.getId() == null) {
            entity = EmailVerificationJpaEntity.builder()
                    .email(domain.getEmail())
                    .code(domain.getCode())
                    .purpose(domain.getPurpose())
                    .expiresAt(domain.getExpiresAt())
                    .build();
        } else {
            entity = jpaRepository.findById(domain.getId())
                    .orElseThrow(() -> new IllegalStateException("EmailVerification entity not found for update: " + domain.getId()));

            entity.updateFromDomain(domain);
        }

        jpaRepository.save(entity);
    }

    @Override
    public Optional<EmailVerification> findLatestByEmailAndPurpose(String email, EmailPurpose purpose) {
        return jpaRepository.findFirstByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .map(this::toDomain);
    }

    @Override
    public Optional<EmailVerification> findLatestPendingByEmailAndPurpose(String email, EmailPurpose purpose) {
        return jpaRepository.findFirstByEmailAndPurposeAndStatusOrderByCreatedAtDesc(
                        email,
                        purpose,
                        VerificationStatus.PENDING
                )
                .map(this::toDomain);
    }

    @Override
    public Optional<EmailVerification> findByVerificationTokenAndPurpose(String token, EmailPurpose purpose) {
        return jpaRepository.findByVerificationTokenAndPurpose(token, purpose)
                .map(this::toDomain);
    }

    @Override
    public Optional<EmailVerification> findValidToken(
            String email,
            String token,
            EmailPurpose purpose,
            LocalDateTime now
    ) {
        return jpaRepository.findFirstByEmailAndVerificationTokenAndPurposeAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
                        email,
                        token,
                        purpose,
                        VerificationStatus.VERIFIED,
                        now
                )
                .map(this::toDomain);
    }

    @Override
    public long countByEmailAndPurposeAndCreatedAtAfter(String email, EmailPurpose purpose, LocalDateTime after) {
        return jpaRepository.countByEmailAndPurposeAndCreatedAtAfter(email, purpose, after);
    }

    @Override
    public void revokeActiveByEmailAndPurpose(String email, EmailPurpose purpose) {
        List<EmailVerificationJpaEntity> activeEntities =
                jpaRepository.findByEmailAndPurposeAndStatusIn(
                        email,
                        purpose,
                        List.of(VerificationStatus.PENDING, VerificationStatus.VERIFIED)
                );

        activeEntities.forEach(entity -> {
            EmailVerification domain = toDomain(entity);
            domain.revoke();
            entity.updateFromDomain(domain);
        });

        jpaRepository.saveAll(activeEntities);
    }

    @Override
    public long deleteByExpiresAtBefore(LocalDateTime cutoff) {
        return jpaRepository.deleteByExpiresAtBefore(cutoff);
    }

    private EmailVerification toDomain(EmailVerificationJpaEntity entity) {
        return EmailVerification.restore(
                entity.getId(),
                entity.getEmail(),
                entity.getCode(),
                entity.getPurpose(),
                entity.getStatus(),
                entity.getVerificationToken(),
                entity.getExpiresAt(),
                entity.getVerifiedAt()
        );
    }
}
