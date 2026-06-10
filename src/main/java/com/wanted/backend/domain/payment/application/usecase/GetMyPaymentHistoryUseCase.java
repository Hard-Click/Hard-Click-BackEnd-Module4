package com.wanted.backend.domain.payment.application.usecase;

import com.wanted.backend.domain.payment.domain.model.PaymentStatus;
import com.wanted.backend.domain.payment.domain.model.PaymentType;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface GetMyPaymentHistoryUseCase {

    Page<MyPaymentHistoryView> handle(Long memberId, int page, int size);

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
