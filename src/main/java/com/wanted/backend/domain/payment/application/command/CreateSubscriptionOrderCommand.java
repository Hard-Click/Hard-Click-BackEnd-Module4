package com.wanted.backend.domain.payment.application.command;

public record CreateSubscriptionOrderCommand(
        Long memberId,
        Long planId
) {}
