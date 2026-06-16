package com.wanted.backend.domain.payment.infrastructure.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubscriptionRefundReferenceRepository extends JpaRepository<SubscriptionRefundReferenceEntity, Long> {

    Optional<SubscriptionRefundReferenceEntity> findByMemberIdAndStatus(Long memberId, String status);

    @Modifying
    @Query("UPDATE SubscriptionRefundReference s SET s.status = :status WHERE s.id = :subscriptionId")
    void updateStatus(@Param("subscriptionId") Long subscriptionId, @Param("status") String status);
}
