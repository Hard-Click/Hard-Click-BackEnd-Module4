package com.wanted.backend.domain.payment.infrastructure.persistence;

import com.wanted.backend.domain.payment.application.port.PaymentRefundQueryPort;
import com.wanted.backend.domain.payment.application.port.PaymentStatusUpdatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentStatusUpdateAdapter implements PaymentStatusUpdatePort, PaymentRefundQueryPort {

    private final PaymentJpaRepository repository;

    @Override
    @Transactional
    public void updateStatus(Long paymentId, String status) {
        repository.updateStatus(paymentId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findPaymentIdByOrderId(Long orderId) {
        return repository.findByOrderId(orderId).map(PaymentJpaEntity::getId);
    }
}
