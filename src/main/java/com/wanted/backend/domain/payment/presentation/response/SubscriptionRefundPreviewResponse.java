package com.wanted.backend.domain.payment.presentation.response;

import com.wanted.backend.domain.payment.application.usecase.GetSubscriptionRefundPreviewUseCase;

import java.time.LocalDateTime;

public record SubscriptionRefundPreviewResponse(
        Long subscriptionId,
        Integer paidAmount,
        Integer refundAmount,
        long remainingDays,
        long totalDays,
        LocalDateTime expiredAt
) {
    public static SubscriptionRefundPreviewResponse from(GetSubscriptionRefundPreviewUseCase.Preview preview) {
        return new SubscriptionRefundPreviewResponse(
                preview.subscriptionId(),
                preview.paidAmount(),
                preview.refundAmount(),
                preview.remainingDays(),
                preview.totalDays(),
                preview.expiredAt()
        );
    }
}
