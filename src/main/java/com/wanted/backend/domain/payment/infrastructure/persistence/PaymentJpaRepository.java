package com.wanted.backend.domain.payment.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, Long>,
        JpaSpecificationExecutor<PaymentJpaEntity> {

    Page<PaymentJpaEntity> findByMemberId(Long memberId, Pageable pageable);

    Optional<PaymentJpaEntity> findByIdempotencyKey(String idempotencyKey);
}
