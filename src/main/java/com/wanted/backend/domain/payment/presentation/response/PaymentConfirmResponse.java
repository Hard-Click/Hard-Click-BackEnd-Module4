package com.wanted.backend.domain.payment.presentation.response;

import com.wanted.backend.domain.order.application.usecase.ConfirmOrderPaymentUseCase;

public record PaymentConfirmResponse(
        String orderNo,
        String status,
        String pgTransactionId,
        boolean duplicate
) {
    public static PaymentConfirmResponse from(ConfirmOrderPaymentUseCase.Result result) {
        return new PaymentConfirmResponse(
                result.orderNo(), result.status().name(), result.pgTransactionId(), result.duplicate());
    }
}
