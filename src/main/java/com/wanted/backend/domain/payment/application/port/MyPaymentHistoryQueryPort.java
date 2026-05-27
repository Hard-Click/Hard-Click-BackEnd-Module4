package com.wanted.backend.domain.payment.application.port;

import com.wanted.backend.domain.payment.domain.model.PaymentStatus;
import com.wanted.backend.domain.payment.domain.model.PaymentType;

import java.time.LocalDateTime;
import java.util.List;

public interface MyPaymentHistoryQueryPort {

    List<MyPaymentHistoryData> findByMemberId(Long memberId);

    record MyPaymentHistoryData(
            Long paymentId,
            Long orderId,
            String orderNo,
            PaymentType paymentType,
            Integer amount,
            PaymentStatus status,
            LocalDateTime paidAt,
            List<Long> courseIds,
            Long subscriptionPlanId
    ) {
    }
}
