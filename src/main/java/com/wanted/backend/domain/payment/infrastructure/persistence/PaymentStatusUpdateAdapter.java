package com.wanted.backend.domain.payment.infrastructure.persistence;

import com.wanted.backend.domain.payment.application.port.PaymentStatusUpdatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentStatusUpdateAdapter implements PaymentStatusUpdatePort {

    private final PaymentJpaRepository repository;

    @Override
    @Transactional
    public void updateStatus(Long paymentId, String status) {
        repository.updateStatus(paymentId, status);
    }
}
