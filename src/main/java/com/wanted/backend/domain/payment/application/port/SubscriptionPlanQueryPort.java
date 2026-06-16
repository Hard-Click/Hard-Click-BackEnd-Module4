package com.wanted.backend.domain.payment.application.port;

public interface SubscriptionPlanQueryPort {
    java.util.Optional<PlanInfo> findById(Long planId);

    record PlanInfo(Long planId, String name, Integer price, int durationMonths) {}
}
