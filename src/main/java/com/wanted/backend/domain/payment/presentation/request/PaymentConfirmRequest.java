package com.wanted.backend.domain.payment.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentConfirmRequest(
        @NotNull
        @Positive
        Integer amount,

        @NotBlank
        String paymentKey,

        @NotBlank
        String orderId
) {
}
