package com.wanted.backend.domain.payment.infrastructure.persistence;

import com.wanted.backend.domain.payment.application.service.ConfirmPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PaymentCreateAdapter implements ConfirmPaymentService.PaymentCreatePort {

    private final WritablePaymentJpaRepository repository;

    @Override
    public Long create(Long orderId, Long memberId, Integer amount,
                       String tossPaymentKey, LocalDateTime paidAt) {
        WritablePaymentJpaEntity entity = WritablePaymentJpaEntity.create(
                orderId, memberId, amount, tossPaymentKey, paidAt);
        return repository.save(entity).getId();
    }
}
