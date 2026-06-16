package com.wanted.backend.domain.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemJpaEntity, Long> {

    List<OrderItemJpaEntity> findByOrderIdIn(Collection<Long> orderIds);

    List<OrderItemJpaEntity> findByCourseId(Long courseId);
}
