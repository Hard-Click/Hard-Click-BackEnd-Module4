package com.wanted.backend.domain.ranking.domain.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RankingTopPercentPolicyTest {

    private final RankingTopPercentPolicy policy = new RankingTopPercentPolicy();

    @Test
    void calculatesTopPercent() {
        assertThat(policy.calculate(12L, 200L)).isEqualTo(6.0);
    }

    @Test
    void returnsZeroWhenRankDoesNotExist() {
        assertThat(policy.calculate(null, 200L)).isEqualTo(0.0);
    }

    @Test
    void returnsZeroWhenTotalUsersDoesNotExist() {
        assertThat(policy.calculate(1L, 0L)).isEqualTo(0.0);
    }
}
