package com.wanted.backend.domain.order.application.usecase;

public interface RefundSubscriptionUseCase {

    void refund(Long memberId, Long orderId, String idempotencyKey);
}
