package com.wanted.backend.domain.payment.application.command;

public record SubscriptionRefundCommand(
        Long memberId,
        String reason
) {}
