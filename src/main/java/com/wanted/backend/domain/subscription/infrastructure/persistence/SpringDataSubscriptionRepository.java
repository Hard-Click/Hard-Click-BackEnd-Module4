package com.wanted.backend.domain.subscription.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataSubscriptionRepository extends JpaRepository<SubscriptionJpaEntity, Long> {
    Optional<SubscriptionJpaEntity> findFirstByMemberIdAndStatusOrderByStartedAtDesc(Long memberId, String status);

    Optional<SubscriptionJpaEntity> findFirstByMemberIdOrderByStartedAtDesc(Long memberId);
}
