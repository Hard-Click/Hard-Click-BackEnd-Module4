package com.wanted.backend.domain.order.domain.repository;

import com.wanted.backend.domain.order.domain.model.Order;

import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long orderId);

    Optional<Order> findByOrderNo(String orderNo);
}
