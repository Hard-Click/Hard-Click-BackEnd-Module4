package com.wanted.backend.domain.learning_activity.infrastructure.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface SpringDataSubscriptionAccessRepository extends JpaRepository<SubscriptionReferenceJpaEntity, Long> {

    boolean existsByMemberIdAndStatusAndExpiredAtGreaterThanEqual(
            Long memberId,
            SubscriptionStatus status,
            LocalDateTime now
    );
}
