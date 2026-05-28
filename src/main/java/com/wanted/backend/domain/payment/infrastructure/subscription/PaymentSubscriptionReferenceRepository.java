package com.wanted.backend.domain.payment.infrastructure.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PaymentSubscriptionReferenceRepository extends JpaRepository<PaymentSubscriptionReferenceEntity, Long> {

    List<PaymentSubscriptionReferenceEntity> findByOrderIdIn(Collection<Long> orderIds);
}
