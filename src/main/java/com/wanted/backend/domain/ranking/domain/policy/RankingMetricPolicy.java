package com.wanted.backend.domain.ranking.domain.policy;

import com.wanted.backend.domain.ranking.domain.model.RankingMetric;

import java.util.Arrays;

public class RankingMetricPolicy {

    public RankingMetric resolve(String metric) {
        if (metric == null || metric.isBlank()) {
            return RankingMetric.STUDY_TIME;
        }

        String normalizedMetric = metric.trim();
        return Arrays.stream(RankingMetric.values())
                .filter(candidate -> candidate.key().equalsIgnoreCase(normalizedMetric))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("랭킹 기준은 study-time, lessons, accepted-comments 중 하나여야 합니다."));
    }
}
