package com.wanted.backend.domain.payment.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, Long> {

    Page<PaymentJpaEntity> findByMemberId(Long memberId, Pageable pageable);
}
