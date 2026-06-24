package com.wanted.backend.domain.subscription.application.service;

import com.wanted.backend.domain.subscription.application.usecase.CancelSubscriptionUseCase;
import com.wanted.backend.domain.subscription.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CancelSubscriptionService implements CancelSubscriptionUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final Clock clock;

    @Override
    public void handle(Long memberId) {
        subscriptionRepository.cancelActiveByMemberId(memberId, LocalDateTime.now(clock));
    }
}
