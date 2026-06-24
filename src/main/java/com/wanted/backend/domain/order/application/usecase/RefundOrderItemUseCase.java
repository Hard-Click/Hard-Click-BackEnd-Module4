package com.wanted.backend.domain.order.application.usecase;

public interface RefundOrderItemUseCase {

    void refund(Long memberId, Long orderId, Long courseId, String idempotencyKey);
}
