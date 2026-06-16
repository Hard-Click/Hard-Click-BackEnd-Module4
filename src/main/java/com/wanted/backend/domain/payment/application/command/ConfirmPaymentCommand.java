package com.wanted.backend.domain.payment.application.command;

public record ConfirmPaymentCommand(
        Long memberId,
        String paymentKey,
        String orderId,
        Integer amount
) {}
