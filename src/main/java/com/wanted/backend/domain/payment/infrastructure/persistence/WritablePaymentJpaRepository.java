package com.wanted.backend.domain.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WritablePaymentJpaRepository extends JpaRepository<WritablePaymentJpaEntity, Long> {
}
