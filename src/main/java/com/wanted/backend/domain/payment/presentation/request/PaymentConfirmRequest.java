package com.wanted.backend.domain.payment.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "결제 확정 요청")
public record PaymentConfirmRequest(
        @Schema(description = "결제 금액 (원)", example = "89000")
        @NotNull
        @Positive
        Integer amount,

        @Schema(description = "Toss 결제 키", example = "tgen_20260520AbCdEfGhIj")
        @NotBlank
        String paymentKey,

        @Schema(description = "주문 번호", example = "ORD-20260520-1A2B3C4D")
        @NotBlank
        String orderId
) {
}
