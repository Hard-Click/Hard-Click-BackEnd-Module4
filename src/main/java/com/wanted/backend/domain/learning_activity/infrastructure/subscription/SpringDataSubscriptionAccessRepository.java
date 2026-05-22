package com.wanted.backend.domain.learning_activity.infrastructure.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataSubscriptionAccessRepository extends JpaRepository<SubscriptionReferenceJpaEntity, Long> {

    @Query("""
            SELECT COUNT(s) > 0
            FROM SubscriptionReferenceJpaEntity s
            WHERE s.memberId = :memberId
              AND s.status = 'ACTIVE'
              AND s.expiredAt >= CURRENT_TIMESTAMP
            """)
    boolean existsActiveSubscription(@Param("memberId") Long memberId);
}
