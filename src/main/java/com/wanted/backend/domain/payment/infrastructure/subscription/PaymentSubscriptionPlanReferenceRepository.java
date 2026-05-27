package com.wanted.backend.domain.payment.infrastructure.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PaymentSubscriptionPlanReferenceRepository extends JpaRepository<PaymentSubscriptionPlanReferenceEntity, Long> {

    List<PaymentSubscriptionPlanReferenceEntity> findByIdIn(Collection<Long> planIds);
}
