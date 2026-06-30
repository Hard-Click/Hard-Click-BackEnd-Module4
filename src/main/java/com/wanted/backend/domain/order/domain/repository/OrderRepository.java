package com.wanted.backend.domain.order.domain.repository;

import com.wanted.backend.domain.order.domain.model.Order;
import com.wanted.backend.domain.order.domain.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long orderId);

    Optional<Order> findByIdForUpdate(Long orderId);

    Optional<Order> findByOrderNo(String orderNo);

    void markPaid(String orderNo, LocalDateTime paidAt, String paymentKey);

    void refundItem(Long orderId, Long courseId, OrderStatus newOrderStatus);

    void refundSubscription(Long orderId);
}
