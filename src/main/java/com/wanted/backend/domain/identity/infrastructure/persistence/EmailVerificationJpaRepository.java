package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.identity.domain.model.EmailPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerificationJpaEntity, Long> {
    // 가장 최근에 생성된 인증 내역 1건 조회
    Optional<EmailVerificationJpaEntity> findFirstByEmailAndPurposeOrderByCreatedAtDesc(String email, EmailPurpose purpose);
    long countByEmailAndPurposeAndCreatedAtAfter(String email, EmailPurpose purpose, LocalDateTime startOfDay);
}