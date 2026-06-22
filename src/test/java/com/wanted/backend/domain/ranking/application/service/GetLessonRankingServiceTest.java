package com.wanted.backend.domain.ranking.application.service;

import com.wanted.backend.domain.ranking.application.port.RankingListReader;
import com.wanted.backend.domain.ranking.application.query.GetLessonRankingQuery;
import com.wanted.backend.domain.ranking.application.usecase.GetLessonRankingUseCase;
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

class GetLessonRankingServiceTest {

    private RankingListReader rankingListReader;
    private GetLessonRankingService service;

    @BeforeEach
    void setUp() {
        rankingListReader = mock(RankingListReader.class);
        service = new GetLessonRankingService(
                rankingListReader,
                new RankingPeriodPolicy()
        );
    }

    @Test
    void returnsLessonRanking() {
        when(rankingListReader.findByMetricAndPeriod(RankingMetric.LESSON, RankingPeriod.DAILY))
                .thenReturn(new RankingList(
                        2L,
                        List.of(
                                new RankingEntry(1L, 1L, 12L),
                                new RankingEntry(2L, 2L, 8L)
                        )
                ));

        GetLessonRankingUseCase.LessonRankingView result =
                service.handle(new GetLessonRankingQuery("daily"));

        assertThat(result.period()).isEqualTo("daily");
        assertThat(result.totalUsers()).isEqualTo(2L);
        assertThat(result.rankings()).hasSize(2);
        assertThat(result.rankings().get(0).rank()).isEqualTo(1L);
        assertThat(result.rankings().get(0).memberId()).isEqualTo(1L);
        assertThat(result.rankings().get(0).watchedLessonCount()).isEqualTo(12L);
        verify(rankingListReader).findByMetricAndPeriod(RankingMetric.LESSON, RankingPeriod.DAILY);
    }

    @Test
    void usesMonthlyPeriodByDefaultWhenPeriodIsNull() {
        when(rankingListReader.findByMetricAndPeriod(RankingMetric.LESSON, RankingPeriod.MONTHLY))
                .thenReturn(RankingList.empty(0L));

        GetLessonRankingUseCase.LessonRankingView result =
                service.handle(new GetLessonRankingQuery(null));

        assertThat(result.period()).isEqualTo("monthly");
        assertThat(result.totalUsers()).isZero();
        assertThat(result.rankings()).isEmpty();
        verify(rankingListReader).findByMetricAndPeriod(RankingMetric.LESSON, RankingPeriod.MONTHLY);
    }

    @Test
    void usesMonthlyPeriodByDefaultWhenPeriodIsBlank() {
        when(rankingListReader.findByMetricAndPeriod(RankingMetric.LESSON, RankingPeriod.MONTHLY))
                .thenReturn(RankingList.empty(0L));

        GetLessonRankingUseCase.LessonRankingView result =
                service.handle(new GetLessonRankingQuery(" "));

        assertThat(result.period()).isEqualTo("monthly");
        assertThat(result.totalUsers()).isZero();
        assertThat(result.rankings()).isEmpty();
        verify(rankingListReader).findByMetricAndPeriod(RankingMetric.LESSON, RankingPeriod.MONTHLY);
    }

    @Test
    void returnsEmptyRankingWhenDataDoesNotExist() {
        when(rankingListReader.findByMetricAndPeriod(RankingMetric.LESSON, RankingPeriod.WEEKLY))
                .thenReturn(RankingList.empty(0L));

        GetLessonRankingUseCase.LessonRankingView result =
                service.handle(new GetLessonRankingQuery("weekly"));

        assertThat(result.period()).isEqualTo("weekly");
        assertThat(result.totalUsers()).isZero();
        assertThat(result.rankings()).isEmpty();
    }

    @Test
    void propagatesExceptionWhenRankingListReaderFails() {
        IllegalStateException exception = new IllegalStateException("Redis connection failed");
        when(rankingListReader.findByMetricAndPeriod(RankingMetric.LESSON, RankingPeriod.DAILY))
                .thenThrow(exception);

        assertThatThrownBy(() -> service.handle(new GetLessonRankingQuery("daily")))
                .isSameAs(exception);

        verify(rankingListReader).findByMetricAndPeriod(RankingMetric.LESSON, RankingPeriod.DAILY);
    }

    @Test
    void rejectsInvalidPeriod() {
        assertThatThrownBy(() -> service.handle(new GetLessonRankingQuery("yearly")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("랭킹 기간은 daily, weekly, monthly 중 하나여야 합니다.");

        verifyNoInteractions(rankingListReader);
    }
}
