package com.wanted.backend.domain.payment.presentation.response;

import com.wanted.backend.domain.payment.application.usecase.GetMyPaymentHistoryUseCase;
import com.wanted.backend.domain.payment.domain.model.PaymentStatus;
import com.wanted.backend.domain.payment.domain.model.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record MyPaymentHistoryResponse(
        @Schema(description = "결제 ID", example = "3001")
        Long paymentId,

        @Schema(description = "주문 ID", example = "2001")
        Long orderId,

        @Schema(description = "주문번호", example = "ORD-20260425-002")
        String orderNo,

        @Schema(description = "결제 유형", example = "COURSE")
        PaymentType paymentType,

        @Schema(description = "결제 금액", example = "99000")
        Integer amount,

        @Schema(description = "결제 상태", example = "PAID")
        PaymentStatus status,

        @Schema(description = "결제 일시", example = "2026-04-25T09:15:00")
        LocalDateTime paidAt,

        @Schema(description = "구매 내역 표시명", example = "TypeScript 심화 학습, Node.js 백엔드 개발")
        String displayName
) {

    public static MyPaymentHistoryResponse from(GetMyPaymentHistoryUseCase.MyPaymentHistoryView view) {
        return new MyPaymentHistoryResponse(
                view.paymentId(),
                view.orderId(),
                view.orderNo(),
                view.paymentType(),
                view.amount(),
                view.status(),
                view.paidAt(),
                view.displayName()
        );
    }

    public static List<MyPaymentHistoryResponse> from(List<GetMyPaymentHistoryUseCase.MyPaymentHistoryView> views) {
        return views.stream()
                .map(MyPaymentHistoryResponse::from)
                .toList();
    }
}
