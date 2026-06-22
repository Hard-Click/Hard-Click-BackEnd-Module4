package com.wanted.backend.domain.ranking.application.service;

import com.wanted.backend.domain.ranking.application.port.RankingListReader;
import com.wanted.backend.domain.ranking.application.query.GetAcceptedCommentRankingQuery;
import com.wanted.backend.domain.ranking.application.usecase.GetAcceptedCommentRankingUseCase;
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

class GetAcceptedCommentRankingServiceTest {

    private RankingListReader rankingListReader;
    private GetAcceptedCommentRankingService service;

    @BeforeEach
    void setUp() {
        rankingListReader = mock(RankingListReader.class);
        service = new GetAcceptedCommentRankingService(
                rankingListReader,
                new RankingPeriodPolicy()
        );
    }

    @Test
    void returnsAcceptedCommentRanking() {
        when(rankingListReader.findByMetricAndPeriod(RankingMetric.ACCEPTED_COMMENT, RankingPeriod.DAILY))
                .thenReturn(new RankingList(
                        2L,
                        List.of(
                                new RankingEntry(1L, 1L, 7L),
                                new RankingEntry(2L, 2L, 4L)
                        )
                ));

        GetAcceptedCommentRankingUseCase.AcceptedCommentRankingView result =
                service.handle(new GetAcceptedCommentRankingQuery("daily"));

        assertThat(result.period()).isEqualTo("daily");
        assertThat(result.totalUsers()).isEqualTo(2L);
        assertThat(result.rankings()).hasSize(2);
        assertThat(result.rankings().get(0).rank()).isEqualTo(1L);
        assertThat(result.rankings().get(0).memberId()).isEqualTo(1L);
        assertThat(result.rankings().get(0).acceptedCommentCount()).isEqualTo(7L);
        verify(rankingListReader).findByMetricAndPeriod(RankingMetric.ACCEPTED_COMMENT, RankingPeriod.DAILY);
    }

    @Test
    void returnsEmptyRankingWhenDataDoesNotExist() {
        when(rankingListReader.findByMetricAndPeriod(RankingMetric.ACCEPTED_COMMENT, RankingPeriod.WEEKLY))
                .thenReturn(RankingList.empty(0L));

        GetAcceptedCommentRankingUseCase.AcceptedCommentRankingView result =
                service.handle(new GetAcceptedCommentRankingQuery("weekly"));

        assertThat(result.period()).isEqualTo("weekly");
        assertThat(result.totalUsers()).isZero();
        assertThat(result.rankings()).isEmpty();
    }

    @Test
    void rejectsInvalidPeriod() {
        assertThatThrownBy(() -> service.handle(new GetAcceptedCommentRankingQuery("yearly")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("랭킹 기간은 daily, weekly, monthly 중 하나여야 합니다.");

        verifyNoInteractions(rankingListReader);
    }
}
