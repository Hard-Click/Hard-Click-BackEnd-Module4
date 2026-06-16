package com.wanted.backend.domain.payment.application.port;

import java.util.Optional;

public interface PaymentRefundQueryPort {

    Optional<Long> findPaymentIdByOrderId(Long orderId);
}
