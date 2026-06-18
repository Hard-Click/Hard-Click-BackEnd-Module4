package com.wanted.backend.domain.payment.presentation.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentConfirmRequest(
        @NotNull
        @Positive
        Long courseId,

        @NotNull
        @Positive
        Integer amount
) {
}
