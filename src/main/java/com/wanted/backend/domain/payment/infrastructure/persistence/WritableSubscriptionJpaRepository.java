package com.wanted.backend.domain.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WritableSubscriptionJpaRepository extends JpaRepository<WritableSubscriptionJpaEntity, Long> {
}
