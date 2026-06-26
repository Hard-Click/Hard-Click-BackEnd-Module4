package com.wanted.backend.domain.subscription.application.dto;

import java.time.LocalDate;
import java.util.List;

public record SubscriptionPlanResult(
        Long planId,
        String name,
        int price,
        int durationDays,
        long daysUntilSuneung,
        LocalDate suneungDate,
        List<String> benefits
) {
}
