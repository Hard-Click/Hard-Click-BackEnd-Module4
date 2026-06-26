package com.wanted.backend.domain.payment.application.port;

import com.wanted.backend.domain.payment.domain.model.PaymentStatus;
import com.wanted.backend.domain.payment.domain.model.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AdminPaymentQueryPort {

    Page<AdminPaymentData> search(PaymentStatus status, String keyword, Pageable pageable);

    record AdminPaymentData(
            Long paymentId,
            Long orderId,
            String orderNo,
            PaymentType paymentType,
            String memberName,
            String memberEmail,
            Integer amount,
            PaymentStatus status,
            LocalDateTime paidAt
    ) {}
}
