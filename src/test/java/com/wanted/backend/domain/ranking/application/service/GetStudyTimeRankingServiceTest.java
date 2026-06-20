package com.wanted.backend.domain.ranking.application.service;

import com.wanted.backend.domain.ranking.application.port.RankingListReader;
import com.wanted.backend.domain.ranking.application.query.GetStudyTimeRankingQuery;
import com.wanted.backend.domain.ranking.application.usecase.GetStudyTimeRankingUseCase;
import com.wanted.backend.domain.ranking.domain.model.RankingEntry;
import com.wanted.backend.domain.ranking.domain.model.RankingList;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
import com.wanted.backend.domain.ranking.domain.policy.RankingPeriodPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GetStudyTimeRankingServiceTest {

    private RankingListReader rankingListReader;
    private GetStudyTimeRankingService service;

    @BeforeEach
    void setUp() {
        rankingListReader = mock(RankingListReader.class);
        service = new GetStudyTimeRankingService(
                rankingListReader,
                new RankingPeriodPolicy()
        );
    }

    @Test
    void returnsStudyTimeRanking() {
        when(rankingListReader.findByMetricAndPeriod(RankingMetric.STUDY_TIME, RankingPeriod.DAILY))
                .thenReturn(new RankingList(
                        2L,
                        List.of(
                                new RankingEntry(1L, 1L, 7200L),
                                new RankingEntry(2L, 2L, 3600L)
                        )
                ));

        GetStudyTimeRankingUseCase.StudyTimeRankingView result =
                service.handle(new GetStudyTimeRankingQuery("daily"));

        assertThat(result.period()).isEqualTo("daily");
        assertThat(result.totalUsers()).isEqualTo(2L);
        assertThat(result.rankings()).hasSize(2);
        assertThat(result.rankings().get(0).rank()).isEqualTo(1L);
        assertThat(result.rankings().get(0).memberId()).isEqualTo(1L);
        assertThat(result.rankings().get(0).studySeconds()).isEqualTo(7200L);
        verify(rankingListReader).findByMetricAndPeriod(RankingMetric.STUDY_TIME, RankingPeriod.DAILY);
    }

    @Test
    void usesMonthlyPeriodByDefault() {
        when(rankingListReader.findByMetricAndPeriod(RankingMetric.STUDY_TIME, RankingPeriod.MONTHLY))
                .thenReturn(RankingList.empty(0L));

        GetStudyTimeRankingUseCase.StudyTimeRankingView result =
                service.handle(new GetStudyTimeRankingQuery(null));

        assertThat(result.period()).isEqualTo("monthly");
        assertThat(result.totalUsers()).isZero();
        assertThat(result.rankings()).isEmpty();
        verify(rankingListReader).findByMetricAndPeriod(RankingMetric.STUDY_TIME, RankingPeriod.MONTHLY);
    }

    @Test
    void returnsEmptyRankingWhenDataDoesNotExist() {
        when(rankingListReader.findByMetricAndPeriod(RankingMetric.STUDY_TIME, RankingPeriod.WEEKLY))
                .thenReturn(RankingList.empty(0L));

        GetStudyTimeRankingUseCase.StudyTimeRankingView result =
                service.handle(new GetStudyTimeRankingQuery("weekly"));

        assertThat(result.period()).isEqualTo("weekly");
        assertThat(result.totalUsers()).isZero();
        assertThat(result.rankings()).isEmpty();
    }

    @Test
    void rejectsInvalidPeriod() {
        assertThatThrownBy(() -> service.handle(new GetStudyTimeRankingQuery("yearly")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("랭킹 기간은 daily, weekly, monthly 중 하나여야 합니다.");

        verifyNoInteractions(rankingListReader);
    }
}
