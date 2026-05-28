package com.wanted.backend.domain.payment.infrastructure.subscription;

import com.wanted.backend.domain.payment.application.port.PaymentSubscriptionPlanDisplayNamePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentSubscriptionPlanDisplayNameAdapter implements PaymentSubscriptionPlanDisplayNamePort {

    private final PaymentSubscriptionPlanReferenceRepository subscriptionPlanRepository;

    @Override
    public Map<Long, String> findNamesByPlanIds(Collection<Long> planIds) {
        if (planIds.isEmpty()) {
            return Map.of();
        }

        return subscriptionPlanRepository.findByIdIn(planIds).stream()
                .collect(Collectors.toMap(PaymentSubscriptionPlanReferenceEntity::getId, PaymentSubscriptionPlanReferenceEntity::getName));
    }
}
