package com.wanted.backend.domain.payment.application.usecase;

import com.wanted.backend.domain.payment.application.command.ConfirmPaymentCommand;
import com.wanted.backend.domain.payment.domain.model.PaymentType;

import java.time.LocalDateTime;

public interface ConfirmPaymentUseCase {
    Result handle(ConfirmPaymentCommand command);

    record Result(
            Long paymentId,
            Long orderId,
            String orderNo,
            Integer amount,
            PaymentType paymentType,
            LocalDateTime paidAt
    ) {}
}
