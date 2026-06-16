package com.wanted.backend.domain.payment.presentation.response;

import com.wanted.backend.domain.payment.application.usecase.RefundSubscriptionUseCase;

import java.time.LocalDateTime;

public record SubscriptionRefundResponse(
        Long refundId,
        Long subscriptionId,
        Integer refundAmount,
        LocalDateTime refundedAt
) {
    public static SubscriptionRefundResponse from(RefundSubscriptionUseCase.Result result) {
        return new SubscriptionRefundResponse(
                result.refundId(),
                result.subscriptionId(),
                result.refundAmount(),
                result.refundedAt()
        );
    }
}
