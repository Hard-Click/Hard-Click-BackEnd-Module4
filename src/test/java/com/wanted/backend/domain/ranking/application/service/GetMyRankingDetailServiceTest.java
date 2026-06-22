package com.wanted.backend.domain.ranking.application.service;

import com.wanted.backend.domain.ranking.application.port.RankingDetailReader;
import com.wanted.backend.domain.ranking.application.query.GetMyRankingDetailQuery;
import com.wanted.backend.domain.ranking.application.usecase.GetMyRankingDetailUseCase.MyRankingDetailView;
import com.wanted.backend.domain.ranking.domain.model.RankingDetail;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
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

class GetMyRankingDetailServiceTest {

    private RankingDetailReader rankingDetailReader;
    private GetMyRankingDetailService service;

    @BeforeEach
    void setUp() {
        rankingDetailReader = mock(RankingDetailReader.class);
        service = new GetMyRankingDetailService(
                rankingDetailReader,
                new RankingMetricPolicy(),
                new RankingPeriodPolicy(),
                new RankingTopPercentPolicy()
        );
    }

    @Test
    void returnsMyRankingDetailBySelectedMetric() {
        when(rankingDetailReader.findByMetricAndPeriodAndMemberId(RankingMetric.LESSON, RankingPeriod.MONTHLY, 1L))
                .thenReturn(new RankingDetail(8L, 200L));

        MyRankingDetailView result = service.handle(new GetMyRankingDetailQuery(1L, "lessons", "monthly"));

        assertThat(result.metric()).isEqualTo("lessons");
        assertThat(result.period()).isEqualTo("monthly");
        assertThat(result.rank()).isEqualTo(8L);
        assertThat(result.totalUsers()).isEqualTo(200L);
        assertThat(result.topPercent()).isEqualTo(4.0);
        verify(rankingDetailReader)
                .findByMetricAndPeriodAndMemberId(RankingMetric.LESSON, RankingPeriod.MONTHLY, 1L);
    }

    @Test
    void usesStudyTimeMetricAndMonthlyPeriodByDefault() {
        when(rankingDetailReader.findByMetricAndPeriodAndMemberId(RankingMetric.STUDY_TIME, RankingPeriod.MONTHLY, 1L))
                .thenReturn(RankingDetail.notRanked(200L));

        MyRankingDetailView result = service.handle(new GetMyRankingDetailQuery(1L, null, null));

        assertThat(result.metric()).isEqualTo("study-time");
        assertThat(result.period()).isEqualTo("monthly");
        assertThat(result.rank()).isNull();
        assertThat(result.totalUsers()).isEqualTo(200L);
        assertThat(result.topPercent()).isEqualTo(0.0);
        verify(rankingDetailReader)
                .findByMetricAndPeriodAndMemberId(RankingMetric.STUDY_TIME, RankingPeriod.MONTHLY, 1L);
    }

    @Test
    void usesStudyTimeMetricAndMonthlyPeriodWhenParametersAreBlank() {
        when(rankingDetailReader.findByMetricAndPeriodAndMemberId(RankingMetric.STUDY_TIME, RankingPeriod.MONTHLY, 1L))
                .thenReturn(RankingDetail.notRanked(200L));

        MyRankingDetailView result = service.handle(new GetMyRankingDetailQuery(1L, "", " "));

        assertThat(result.metric()).isEqualTo("study-time");
        assertThat(result.period()).isEqualTo("monthly");
        assertThat(result.rank()).isNull();
        assertThat(result.totalUsers()).isEqualTo(200L);
        assertThat(result.topPercent()).isEqualTo(0.0);
        verify(rankingDetailReader)
                .findByMetricAndPeriodAndMemberId(RankingMetric.STUDY_TIME, RankingPeriod.MONTHLY, 1L);
    }

    @Test
    void rejectsInvalidMetric() {
        assertThatThrownBy(() -> service.handle(new GetMyRankingDetailQuery(1L, "likes", "monthly")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("랭킹 기준은 study-time, lessons, accepted-comments 중 하나여야 합니다.");

        verifyNoInteractions(rankingDetailReader);
    }

    @Test
    void rejectsInvalidPeriod() {
        assertThatThrownBy(() -> service.handle(new GetMyRankingDetailQuery(1L, "study-time", "yearly")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("랭킹 기간은 daily, weekly, monthly 중 하나여야 합니다.");

        verifyNoInteractions(rankingDetailReader);
    }

    @Test
    void rejectsQueryWhenQueryIsNull() {
        assertThatThrownBy(() -> service.handle(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("요청은 필수입니다.");

        verifyNoInteractions(rankingDetailReader);
    }

    @Test
    void rejectsQueryWhenMemberIdIsNull() {
        assertThatThrownBy(() -> service.handle(new GetMyRankingDetailQuery(null, "study-time", "monthly")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 ID는 필수입니다.");

        verifyNoInteractions(rankingDetailReader);
    }
}
