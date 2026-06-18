package com.wanted.backend.domain.payment.domain.repository;

import com.wanted.backend.domain.payment.domain.model.Payment;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Payment confirmPayment(Long paymentId, String pgTransactionId, LocalDateTime paidAt);

    Payment failPayment(Long paymentId);
}
