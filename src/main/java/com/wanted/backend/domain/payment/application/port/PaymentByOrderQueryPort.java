package com.wanted.backend.domain.payment.application.port;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentByOrderQueryPort {

    Optional<PaymentDetail> findByOrderId(Long orderId);

    record PaymentDetail(
            Long paymentId,
            Integer paidAmount,
            LocalDateTime paidAt,
            String status
    ) {}
}
