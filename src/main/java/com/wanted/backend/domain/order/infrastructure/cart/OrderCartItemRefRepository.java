package com.wanted.backend.domain.order.infrastructure.cart;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderCartItemRefRepository extends JpaRepository<OrderCartItemRefEntity, Long> {

    List<OrderCartItemRefEntity> findByMemberId(Long memberId);
}
