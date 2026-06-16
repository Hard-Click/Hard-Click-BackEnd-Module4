package com.wanted.backend.domain.payment.infrastructure.persistence;

import com.wanted.backend.domain.payment.application.port.PaymentByOrderQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentByOrderQueryAdapter implements PaymentByOrderQueryPort {

    private final PaymentJpaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentDetail> findByOrderId(Long orderId) {
        return repository.findByOrderId(orderId)
                .map(p -> new PaymentDetail(p.getId(), p.getPaidAmount(), p.getPaidAt(), p.getStatus()));
    }
}
