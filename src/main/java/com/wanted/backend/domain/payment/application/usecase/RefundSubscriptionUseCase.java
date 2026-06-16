package com.wanted.backend.domain.payment.application.usecase;

import com.wanted.backend.domain.payment.application.command.SubscriptionRefundCommand;

import java.time.LocalDateTime;

public interface RefundSubscriptionUseCase {

    Result handle(SubscriptionRefundCommand command);

    record Result(
            Long refundId,
            Long subscriptionId,
            Integer refundAmount,
            LocalDateTime refundedAt
    ) {}
}
