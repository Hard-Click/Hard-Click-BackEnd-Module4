package com.wanted.backend.domain.subscription.application.dto;

import java.util.List;

public record SubscriptionPlanResult(
        Long planId,
        String name,
        int price,
        int durationDays,
        List<String> benefits
) {
}
