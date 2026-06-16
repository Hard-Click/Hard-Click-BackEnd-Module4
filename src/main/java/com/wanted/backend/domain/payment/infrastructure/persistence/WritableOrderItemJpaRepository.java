package com.wanted.backend.domain.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WritableOrderItemJpaRepository extends JpaRepository<WritableOrderItemJpaEntity, Long> {

    List<WritableOrderItemJpaEntity> findByOrderId(Long orderId);
}
