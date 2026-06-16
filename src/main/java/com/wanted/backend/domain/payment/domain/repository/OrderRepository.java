package com.wanted.backend.domain.payment.domain.repository;

import com.wanted.backend.domain.payment.domain.model.Order;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long orderId);
}
