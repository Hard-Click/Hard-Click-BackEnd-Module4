package com.wanted.backend.domain.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataOrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByOrderNo(String orderNo);
}
