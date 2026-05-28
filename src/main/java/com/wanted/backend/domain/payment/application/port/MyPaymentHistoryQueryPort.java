package com.wanted.backend.domain.payment.application.port;

import com.wanted.backend.domain.payment.domain.model.PaymentStatus;
import com.wanted.backend.domain.payment.domain.model.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface MyPaymentHistoryQueryPort {

    Page<MyPaymentHistoryData> findByMemberId(Long memberId, Pageable pageable);

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
