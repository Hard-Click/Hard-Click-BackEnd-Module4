package com.wanted.backend.domain.ranking.domain.policy;

import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RankingMetricPolicyTest {

    private final RankingMetricPolicy policy = new RankingMetricPolicy();

    @Test
    void resolvesDefaultMetricAsStudyTimeWhenMetricIsMissing() {
        assertThat(policy.resolve(null)).isEqualTo(RankingMetric.STUDY_TIME);
        assertThat(policy.resolve(" ")).isEqualTo(RankingMetric.STUDY_TIME);
    }

    @Test
    void resolvesMetricCaseInsensitively() {
        assertThat(policy.resolve("study-time")).isEqualTo(RankingMetric.STUDY_TIME);
        assertThat(policy.resolve("LESSONS")).isEqualTo(RankingMetric.LESSON);
        assertThat(policy.resolve(" accepted-comments ")).isEqualTo(RankingMetric.ACCEPTED_COMMENT);
    }

    @Test
    void rejectsInvalidMetric() {
        assertThatThrownBy(() -> policy.resolve("likes"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("랭킹 기준은 study-time, lessons, accepted-comments 중 하나여야 합니다.");
    }
}
