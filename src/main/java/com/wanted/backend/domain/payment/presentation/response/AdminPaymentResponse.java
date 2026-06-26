package com.wanted.backend.domain.payment.presentation.response;

import com.wanted.backend.domain.payment.application.port.AdminPaymentQueryPort;
import com.wanted.backend.domain.payment.domain.model.PaymentStatus;
import com.wanted.backend.domain.payment.domain.model.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "관리자 결제 내역 항목")
public record AdminPaymentResponse(
        @Schema(description = "결제 ID", example = "1")
        Long paymentId,

        @Schema(description = "주문번호", example = "ORD-20260520-001")
        String orderNo,

        @Schema(description = "결제 구분 (COURSE / SUBSCRIPTION)", example = "COURSE")
        PaymentType paymentType,

        @Schema(description = "사용자명", example = "김민수")
        String memberName,

        @Schema(description = "이메일", example = "user@example.com")
        String memberEmail,

        @Schema(description = "결제 금액", example = "109000")
        Integer amount,

        @Schema(description = "결제 수단 (현재 PG 연동 전이라 고정값)", example = "카드")
        String paymentMethod,

        @Schema(description = "결제 상태 (PENDING / PAID / REFUNDED / FAILED 등)", example = "PAID")
        PaymentStatus status,

        @Schema(description = "결제일시")
        LocalDateTime paidAt,

        @Schema(description = "환불 가능 여부 (PAID 상태만 true)", example = "true")
        boolean refundable
) {
    private static final String MOCK_PAYMENT_METHOD = "카드";

    public static AdminPaymentResponse from(AdminPaymentQueryPort.AdminPaymentData data) {
        return new AdminPaymentResponse(
                data.paymentId(),
                data.orderNo(),
                data.paymentType(),
                data.memberName(),
                data.memberEmail(),
                data.amount(),
                MOCK_PAYMENT_METHOD,
                data.status(),
                data.paidAt(),
                data.status() == PaymentStatus.PAID
        );
    }
}
