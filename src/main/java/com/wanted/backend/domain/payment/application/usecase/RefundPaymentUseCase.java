package com.wanted.backend.domain.payment.application.usecase;

public interface RefundPaymentUseCase {
    void handle(Long paymentId);
}
