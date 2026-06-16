package com.wanted.backend.domain.payment.presentation.response;

import com.wanted.backend.domain.payment.application.usecase.ConfirmPaymentUseCase;
import com.wanted.backend.domain.payment.domain.model.PaymentType;

import java.time.LocalDateTime;

public record ConfirmPaymentResponse(
        Long paymentId,
        Long orderId,
        String orderNo,
        Integer amount,
        PaymentType paymentType,
        LocalDateTime paidAt
) {
    public static ConfirmPaymentResponse from(ConfirmPaymentUseCase.Result result) {
        return new ConfirmPaymentResponse(
                result.paymentId(), result.orderId(), result.orderNo(),
                result.amount(), result.paymentType(), result.paidAt());
    }
}
