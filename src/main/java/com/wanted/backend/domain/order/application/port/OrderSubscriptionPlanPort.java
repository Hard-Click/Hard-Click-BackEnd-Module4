package com.wanted.backend.domain.order.application.port;

public interface OrderSubscriptionPlanPort {

    PlanInfo getAnnualPass();

    record PlanInfo(Long planId, String name, int price) {}
}
