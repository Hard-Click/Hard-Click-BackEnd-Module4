package com.wanted.backend.domain.payment.application.port;

import java.time.LocalDateTime;

public interface SubscriptionCreatePort {
    void create(Long memberId, Long orderId, Long planId, LocalDateTime expiredAt);
}
