package com.wanted.backend.domain.ranking.domain.policy;

import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;

import java.util.Arrays;

public class RankingPeriodPolicy {

    public RankingPeriod resolve(String period) {
        if (period == null || period.isBlank()) {
            return RankingPeriod.MONTHLY;
        }

        String normalizedPeriod = period.trim();
        return Arrays.stream(RankingPeriod.values())
                .filter(candidate -> candidate.value().equalsIgnoreCase(normalizedPeriod))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("랭킹 기간은 daily, weekly, monthly 중 하나여야 합니다."));
    }
}
