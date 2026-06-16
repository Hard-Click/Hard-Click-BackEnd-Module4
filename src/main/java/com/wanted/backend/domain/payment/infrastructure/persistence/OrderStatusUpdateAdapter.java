package com.wanted.backend.domain.payment.infrastructure.persistence;

import com.wanted.backend.domain.payment.application.port.OrderStatusUpdatePort;
import com.wanted.backend.domain.payment.domain.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderStatusUpdateAdapter implements OrderStatusUpdatePort {

    private final WritableOrderJpaRepository repository;

    @Override
    @Transactional
    public void updateStatus(Long orderId, OrderStatus status) {
        repository.updateStatus(orderId, status);
    }
}
