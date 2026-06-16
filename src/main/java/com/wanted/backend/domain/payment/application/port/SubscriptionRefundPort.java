package com.wanted.backend.domain.payment.application.port;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SubscriptionRefundPort {

    Optional<SubscriptionData> findActiveByMemberId(Long memberId);

    void updateStatusToCancelled(Long subscriptionId);

    record SubscriptionData(
            Long subscriptionId,
            Long orderId,
            Long planId,
            LocalDateTime startedAt,
            LocalDateTime expiredAt,
            Integer paidAmount,
            String status
    ) {}
}
