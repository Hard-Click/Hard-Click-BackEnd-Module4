package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import com.wanted.backend.domain.identity.domain.model.EmailVerification;
import com.wanted.backend.domain.identity.domain.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailVerificationPersistenceAdapter implements EmailVerificationRepository {

    private final EmailVerificationJpaRepository jpaRepository;

    @Override
    public void save(EmailVerification domain) {
        EmailVerificationJpaEntity entity;

        if (domain.getId() == null) {
            // 1. 신규 생성: ID가 없으면 빌더를 통해 새로운 엔티티 생성
            entity = EmailVerificationJpaEntity.builder()
                    .email(domain.getEmail())
                    .code(domain.getCode())
                    .purpose(domain.getPurpose())
                    .expiresAt(domain.getExpiresAt())
                    .build();
        } else {
            // 2. 업데이트: ID가 있으면 DB에서 찾아서 상태 동기화
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
}