package com.wanted.backend.domain.payment.application.usecase;

import com.wanted.backend.domain.payment.domain.model.PaymentStatus;
import com.wanted.backend.domain.payment.domain.model.PaymentType;

import java.time.LocalDateTime;
import java.util.List;

public interface GetMyPaymentHistoryUseCase {

    List<MyPaymentHistoryView> handle(Long memberId);

    record MyPaymentHistoryView(
            Long paymentId,
            Long orderId,
            String orderNo,
            PaymentType paymentType,
            Integer amount,
            PaymentStatus status,
            LocalDateTime paidAt,
            String displayName
    ) {
    }
}
