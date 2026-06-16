package com.wanted.backend.domain.payment.application.usecase;

import java.time.LocalDateTime;

public interface GetSubscriptionRefundPreviewUseCase {

    Preview handle(Long memberId);

    record Preview(
            Long subscriptionId,
            Integer paidAmount,
            Integer refundAmount,
            long remainingDays,
            long totalDays,
            LocalDateTime expiredAt
    ) {}
}
