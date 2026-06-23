package com.wanted.backend.domain.subscription.application.dto;

import java.time.LocalDateTime;

public record MySubscriptionResult(
        boolean subscribed,
        Long subscriptionId,
        Long planId,
        String paymentMethod,
        Integer paidAmount,
        LocalDateTime startedAt,
        LocalDateTime expiredAt,
        long remainingDays
) {
    public static MySubscriptionResult notSubscribed() {
        return new MySubscriptionResult(false, null, null, null, null, null, null, 0);
    }
}
