package com.wanted.backend.domain.order.infrastructure.subscription;

import com.wanted.backend.domain.order.application.port.OrderSubscriptionPlanPort;
import com.wanted.backend.domain.subscription.application.pricing.SubscriptionPricingPolicy;
import com.wanted.backend.domain.subscription.domain.model.SubscriptionPlanCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderSubscriptionPlanAdapter implements OrderSubscriptionPlanPort {

    private final SubscriptionPricingPolicy pricingPolicy;

    @Override
    public PlanInfo getAnnualPass() {
        return new PlanInfo(
                SubscriptionPlanCatalog.ANNUAL_PASS_PLAN_ID,
                SubscriptionPlanCatalog.ANNUAL_PASS_NAME,
                pricingPolicy.currentPrice());
    }
}
