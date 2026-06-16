package com.wanted.backend.domain.payment.application.port;

public interface PaymentStatusUpdatePort {

    void updateStatus(Long paymentId, String status);
}
