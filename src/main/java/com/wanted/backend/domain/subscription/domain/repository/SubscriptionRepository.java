package com.wanted.backend.domain.subscription.domain.repository;

import com.wanted.backend.domain.subscription.domain.model.Subscription;

import java.util.Optional;

public interface SubscriptionRepository {
    Subscription save(Subscription subscription);

    Optional<Subscription> findActiveByMemberId(Long memberId);

    Optional<Subscription> findLatestByMemberId(Long memberId);

    Subscription cancelActiveByMemberId(Long memberId, java.time.LocalDateTime cancelledAt);
}
