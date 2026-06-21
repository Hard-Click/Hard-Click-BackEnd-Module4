package com.wanted.backend.domain.ranking.infrastructure.config;

import com.wanted.backend.domain.ranking.domain.policy.RankingMetricPolicy;
import com.wanted.backend.domain.ranking.domain.policy.RankingPeriodPolicy;
import com.wanted.backend.domain.ranking.domain.policy.RankingTopPercentPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RankingPolicyConfig {

    @Bean
    public RankingPeriodPolicy rankingPeriodPolicy() {
        return new RankingPeriodPolicy();
    }

    @Bean
    public RankingMetricPolicy rankingMetricPolicy() {
        return new RankingMetricPolicy();
    }

    @Bean
    public RankingTopPercentPolicy rankingTopPercentPolicy() {
        return new RankingTopPercentPolicy();
    }
}
