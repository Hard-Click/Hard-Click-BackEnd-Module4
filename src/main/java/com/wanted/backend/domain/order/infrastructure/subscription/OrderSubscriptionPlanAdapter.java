package com.wanted.backend.domain.order.infrastructure.subscription;

import com.wanted.backend.domain.order.application.port.OrderSubscriptionPlanPort;
import com.wanted.backend.domain.subscription.domain.model.SubscriptionPlanCatalog;
import org.springframework.stereotype.Component;

@Component
public class OrderSubscriptionPlanAdapter implements OrderSubscriptionPlanPort {

    @Override
    public PlanInfo getAnnualPass() {
        return new PlanInfo(
                SubscriptionPlanCatalog.ANNUAL_PASS_PLAN_ID,
                SubscriptionPlanCatalog.ANNUAL_PASS_NAME,
                SubscriptionPlanCatalog.ANNUAL_PASS_PRICE);
    }
}
