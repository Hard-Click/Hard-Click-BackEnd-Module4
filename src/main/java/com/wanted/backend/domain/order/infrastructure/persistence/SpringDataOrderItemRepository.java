package com.wanted.backend.domain.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataOrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    List<OrderItemEntity> findByOrderId(Long orderId);

    Optional<OrderItemEntity> findByOrderIdAndCourseId(Long orderId, Long courseId);
}
