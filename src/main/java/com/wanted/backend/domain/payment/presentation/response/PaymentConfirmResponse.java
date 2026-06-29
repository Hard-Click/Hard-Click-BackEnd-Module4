package com.wanted.backend.domain.payment.presentation.response;

import com.wanted.backend.domain.order.application.usecase.ConfirmOrderPaymentUseCase;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 확정 응답")
public record PaymentConfirmResponse(
        @Schema(description = "주문 번호", example = "ORD-20260520-1A2B3C4D")
        String orderNo,

        @Schema(description = "결제 후 주문 상태", example = "PAID")
        String status,

        @Schema(description = "PG사 거래 ID", example = "tgen_20260520AbCdEfGhIj")
        String pgTransactionId,

        @Schema(description = "멱등키 중복 요청 여부 (true이면 이미 처리된 결제)", example = "false")
        boolean duplicate
) {
    public static PaymentConfirmResponse from(ConfirmOrderPaymentUseCase.Result result) {
        return new PaymentConfirmResponse(
                result.orderNo(), result.status().name(), result.pgTransactionId(), result.duplicate());
    }
}
