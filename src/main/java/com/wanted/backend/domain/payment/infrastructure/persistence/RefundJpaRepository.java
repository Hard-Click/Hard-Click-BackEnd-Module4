package com.wanted.backend.domain.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface RefundJpaRepository extends JpaRepository<RefundJpaEntity, Long> {
}
