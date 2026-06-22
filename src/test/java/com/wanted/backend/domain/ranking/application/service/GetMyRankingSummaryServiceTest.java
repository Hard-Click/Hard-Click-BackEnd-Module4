package com.wanted.backend.domain.ranking.application.service;

import com.wanted.backend.domain.ranking.application.port.RankingDetailReader;
import com.wanted.backend.domain.ranking.application.query.GetMyRankingSummaryQuery;
import com.wanted.backend.domain.ranking.application.usecase.GetMyRankingSummaryUseCase.MyRankingSummaryView;
import com.wanted.backend.domain.ranking.domain.model.RankingDetail;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
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

    private RankingDetailReader rankingDetailReader;
    private GetMyRankingSummaryService service;

    @BeforeEach
    void setUp() {
        rankingDetailReader = mock(RankingDetailReader.class);
        service = new GetMyRankingSummaryService(
                rankingDetailReader,
                new RankingTopPercentPolicy()
        );
    }

    @Test
    void returnsMyRankingSummaryByMonthlyRanking() {
        when(rankingDetailReader.findByMetricAndPeriodAndMemberId(RankingMetric.STUDY_TIME, RankingPeriod.MONTHLY, 1L))
                .thenReturn(new RankingDetail(42L, 350L));
        when(rankingDetailReader.findByMetricAndPeriodAndMemberId(RankingMetric.LESSON, RankingPeriod.MONTHLY, 1L))
                .thenReturn(new RankingDetail(38L, 380L));
        when(rankingDetailReader.findByMetricAndPeriodAndMemberId(RankingMetric.ACCEPTED_COMMENT, RankingPeriod.MONTHLY, 1L))
                .thenReturn(new RankingDetail(15L, 300L));

        MyRankingSummaryView result = service.handle(new GetMyRankingSummaryQuery(1L));

        assertThat(result.studyTime().rank()).isEqualTo(42L);
        assertThat(result.studyTime().totalUsers()).isEqualTo(350L);
        assertThat(result.studyTime().topPercent()).isEqualTo(12.0);
        assertThat(result.lesson().rank()).isEqualTo(38L);
        assertThat(result.lesson().totalUsers()).isEqualTo(380L);
        assertThat(result.lesson().topPercent()).isEqualTo(10.0);
        assertThat(result.acceptedComment().rank()).isEqualTo(15L);
        assertThat(result.acceptedComment().totalUsers()).isEqualTo(300L);
        assertThat(result.acceptedComment().topPercent()).isEqualTo(5.0);

        verify(rankingDetailReader)
                .findByMetricAndPeriodAndMemberId(RankingMetric.STUDY_TIME, RankingPeriod.MONTHLY, 1L);
        verify(rankingDetailReader)
                .findByMetricAndPeriodAndMemberId(RankingMetric.LESSON, RankingPeriod.MONTHLY, 1L);
        verify(rankingDetailReader)
                .findByMetricAndPeriodAndMemberId(RankingMetric.ACCEPTED_COMMENT, RankingPeriod.MONTHLY, 1L);
    }

    @Test
    void returnsDefaultValueWhenRankDoesNotExist() {
        when(rankingDetailReader.findByMetricAndPeriodAndMemberId(RankingMetric.STUDY_TIME, RankingPeriod.MONTHLY, 1L))
                .thenReturn(RankingDetail.notRanked(350L));
        when(rankingDetailReader.findByMetricAndPeriodAndMemberId(RankingMetric.LESSON, RankingPeriod.MONTHLY, 1L))
                .thenReturn(RankingDetail.notRanked(0L));
        when(rankingDetailReader.findByMetricAndPeriodAndMemberId(RankingMetric.ACCEPTED_COMMENT, RankingPeriod.MONTHLY, 1L))
                .thenReturn(RankingDetail.notRanked(null));

        MyRankingSummaryView result = service.handle(new GetMyRankingSummaryQuery(1L));

        assertThat(result.studyTime().rank()).isNull();
        assertThat(result.studyTime().totalUsers()).isEqualTo(350L);
        assertThat(result.studyTime().topPercent()).isEqualTo(0.0);
        assertThat(result.lesson().rank()).isNull();
        assertThat(result.lesson().totalUsers()).isZero();
        assertThat(result.lesson().topPercent()).isEqualTo(0.0);
        assertThat(result.acceptedComment().rank()).isNull();
        assertThat(result.acceptedComment().totalUsers()).isZero();
        assertThat(result.acceptedComment().topPercent()).isEqualTo(0.0);
    }

    @Test
    void rejectsQueryWhenMemberIdIsNull() {
        assertThatThrownBy(() -> service.handle(new GetMyRankingSummaryQuery(null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 ID는 필수입니다.");

        verifyNoInteractions(rankingDetailReader);
    }
}
