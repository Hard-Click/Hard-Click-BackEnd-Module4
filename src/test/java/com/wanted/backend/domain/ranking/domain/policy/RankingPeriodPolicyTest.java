package com.wanted.backend.domain.ranking.domain.policy;

import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RankingPeriodPolicyTest {

    private final RankingPeriodPolicy policy = new RankingPeriodPolicy();

    @Test
    void resolvesDefaultPeriodAsMonthlyWhenPeriodIsMissing() {
        assertThat(policy.resolve(null)).isEqualTo(RankingPeriod.MONTHLY);
        assertThat(policy.resolve(" ")).isEqualTo(RankingPeriod.MONTHLY);
    }

    @Test
    void resolvesPeriodCaseInsensitively() {
        assertThat(policy.resolve("daily")).isEqualTo(RankingPeriod.DAILY);
        assertThat(policy.resolve("WEEKLY")).isEqualTo(RankingPeriod.WEEKLY);
        assertThat(policy.resolve(" monthly ")).isEqualTo(RankingPeriod.MONTHLY);
    }

    @Test
    void rejectsInvalidPeriod() {
        assertThatThrownBy(() -> policy.resolve("yearly"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("랭킹 기간은 daily, weekly, monthly 중 하나여야 합니다.");
    }
}
