package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.EmailVerification;
import com.wanted.backend.domain.identity.domain.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
                .map(entity -> EmailVerification.restore(
                        entity.getId(), entity.getEmail(), entity.getCode(), entity.getPurpose(),
                        entity.isVerified(), entity.getVerificationToken(),
                        entity.getExpiresAt(), entity.getVerifiedAt()
                ));
    }


    @Override
    public long countByEmailAndPurposeAndCreatedAtAfter(String email, EmailPurpose purpose, LocalDateTime after) {
        return jpaRepository.countByEmailAndPurposeAndCreatedAtAfter(email, purpose, after);
    }
}