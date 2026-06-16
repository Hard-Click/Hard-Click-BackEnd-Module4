package com.wanted.backend.domain.payment.infrastructure.subscription;

import com.wanted.backend.domain.payment.application.port.SubscriptionCreatePort;
import com.wanted.backend.domain.payment.infrastructure.persistence.WritableSubscriptionJpaEntity;
import com.wanted.backend.domain.payment.infrastructure.persistence.WritableSubscriptionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SubscriptionCreateAdapter implements SubscriptionCreatePort {

    private final WritableSubscriptionJpaRepository repository;

    @Override
    public void create(Long memberId, Long orderId, Long planId, LocalDateTime expiredAt) {
        WritableSubscriptionJpaEntity entity = WritableSubscriptionJpaEntity.create(
                memberId, orderId, planId, expiredAt);
        repository.save(entity);
    }
}
