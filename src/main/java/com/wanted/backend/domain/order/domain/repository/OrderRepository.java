package com.wanted.backend.domain.order.domain.repository;

import com.wanted.backend.domain.order.domain.model.Order;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long orderId);

    Optional<Order> findByIdForUpdate(Long orderId);

    Optional<Order> findByOrderNo(String orderNo);

    void markPaid(String orderNo, LocalDateTime paidAt, String paymentKey);

    /**
     * 단일 항목 환불. 주문 row를 비관적 락으로 잡고, 항목을 환불 처리한 뒤
     * 남은 항목 상태를 DB에서 재조회해 주문 상태(REFUNDED/PARTIAL_REFUNDED)를 재계산한다.
     */
    void refundItem(Long orderId, Long courseId);

    /**
     * 주문 전체 환불(관리자). 주문을 REFUNDED로, 모든 미환불 항목을 환불 처리한다.
     */
    void refundAll(Long orderId);
}
