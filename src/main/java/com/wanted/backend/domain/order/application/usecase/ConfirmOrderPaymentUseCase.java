package com.wanted.backend.domain.order.application.usecase;

import com.wanted.backend.domain.order.domain.model.OrderStatus;

public interface ConfirmOrderPaymentUseCase {

    Result confirm(Long memberId, String orderNo, String paymentKey, Integer amount, String idempotencyKey);

    record Result(String orderNo, OrderStatus status, String pgTransactionId, boolean duplicate) {
    }
}
