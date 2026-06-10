package com.wanted.backend.domain.learning_activity.infrastructure.subscription;

import com.wanted.backend.domain.learning_activity.application.port.SubscriptionAccessPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionAccessAdapter implements SubscriptionAccessPort {

    private final SpringDataSubscriptionAccessRepository repository;

    @Override
    public boolean hasActiveSubscription(Long memberId) {
        return repository.existsByMemberIdAndStatusAndExpiredAtGreaterThanEqual(
                memberId,
                SubscriptionStatus.ACTIVE,
                LocalDateTime.now()
        );
    }
}
