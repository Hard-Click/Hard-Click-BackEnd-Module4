package com.wanted.backend.domain.payment.infrastructure.subscription;

import com.wanted.backend.domain.payment.application.port.SubscriptionPlanQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubscriptionPlanQueryAdapter implements SubscriptionPlanQueryPort {

    private final PaymentSubscriptionPlanReferenceRepository repository;

    @Override
    public Optional<PlanInfo> findById(Long planId) {
        return repository.findById(planId)
                .map(e -> new PlanInfo(e.getId(), e.getName(), e.getPrice(), e.getDurationMonths()));
    }
}
