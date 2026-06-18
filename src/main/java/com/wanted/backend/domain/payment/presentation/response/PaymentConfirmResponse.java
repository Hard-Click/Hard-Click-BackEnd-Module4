package com.wanted.backend.domain.payment.presentation.response;

import com.wanted.backend.domain.payment.application.facade.PaymentFacade;
import com.wanted.backend.domain.payment.domain.model.PaymentStatus;

public record PaymentConfirmResponse(
        PaymentStatus status,
        String pgTransactionId,
        boolean duplicate
) {
    public static PaymentConfirmResponse from(PaymentFacade.Result result) {
        return new PaymentConfirmResponse(result.status(), result.pgTransactionId(), result.duplicate());
    }
}
