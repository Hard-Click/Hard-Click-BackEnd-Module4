package com.wanted.backend.domain.ranking.application.service;

import com.wanted.backend.domain.ranking.application.port.RankingSummaryReader;
import com.wanted.backend.domain.ranking.application.query.GetMyRankingSummaryQuery;
import com.wanted.backend.domain.ranking.application.usecase.GetMyRankingSummaryUseCase.MyRankingSummaryView;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
import com.wanted.backend.domain.ranking.domain.model.RankingSummary;
import com.wanted.backend.domain.ranking.domain.policy.RankingMetricPolicy;
import com.wanted.backend.domain.ranking.domain.policy.RankingPeriodPolicy;
import com.wanted.backend.domain.ranking.domain.policy.RankingTopPercentPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GetMyRankingSummaryServiceTest {

    private RankingSummaryReader rankingSummaryReader;
    private GetMyRankingSummaryService service;

    @BeforeEach
    void setUp() {
        rankingSummaryReader = mock(RankingSummaryReader.class);
        service = new GetMyRankingSummaryService(
                rankingSummaryReader,
                new RankingMetricPolicy(),
                new RankingPeriodPolicy(),
                new RankingTopPercentPolicy()
        );
    }

    @Test
    void returnsMyRankingSummaryBySelectedMetric() {
        when(rankingSummaryReader.findByMetricAndPeriodAndMemberId(RankingMetric.LESSON, RankingPeriod.MONTHLY, 1L))
                .thenReturn(new RankingSummary(8L, 200L));

        MyRankingSummaryView result = service.handle(new GetMyRankingSummaryQuery(1L, "lessons", "monthly"));

        assertThat(result.metric()).isEqualTo("lessons");
        assertThat(result.period()).isEqualTo("monthly");
        assertThat(result.rank()).isEqualTo(8L);
        assertThat(result.totalUsers()).isEqualTo(200L);
        assertThat(result.topPercent()).isEqualTo(4.0);
        verify(rankingSummaryReader)
                .findByMetricAndPeriodAndMemberId(RankingMetric.LESSON, RankingPeriod.MONTHLY, 1L);
    }

    @Test
    void usesStudyTimeMetricAndMonthlyPeriodByDefault() {
        when(rankingSummaryReader.findByMetricAndPeriodAndMemberId(RankingMetric.STUDY_TIME, RankingPeriod.MONTHLY, 1L))
                .thenReturn(RankingSummary.notRanked(200L));

        MyRankingSummaryView result = service.handle(new GetMyRankingSummaryQuery(1L, null, null));

        assertThat(result.metric()).isEqualTo("study-time");
        assertThat(result.period()).isEqualTo("monthly");
        assertThat(result.rank()).isNull();
        assertThat(result.totalUsers()).isEqualTo(200L);
        assertThat(result.topPercent()).isEqualTo(0.0);
        verify(rankingSummaryReader)
                .findByMetricAndPeriodAndMemberId(RankingMetric.STUDY_TIME, RankingPeriod.MONTHLY, 1L);
    }

    @Test
    void rejectsInvalidMetric() {
        assertThatThrownBy(() -> service.handle(new GetMyRankingSummaryQuery(1L, "likes", "monthly")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("랭킹 기준은 study-time, lessons, accepted-comments 중 하나여야 합니다.");

        verifyNoInteractions(rankingSummaryReader);
    }

    @Test
    void rejectsInvalidPeriod() {
        assertThatThrownBy(() -> service.handle(new GetMyRankingSummaryQuery(1L, "study-time", "yearly")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("랭킹 기간은 daily, weekly, monthly 중 하나여야 합니다.");

        verifyNoInteractions(rankingSummaryReader);
    }

    @Test
    void rejectsQueryWhenMemberIdIsNull() {
        assertThatThrownBy(() -> service.handle(new GetMyRankingSummaryQuery(null, "study-time", "monthly")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 ID는 필수입니다.");

        verifyNoInteractions(rankingSummaryReader);
    }
}
