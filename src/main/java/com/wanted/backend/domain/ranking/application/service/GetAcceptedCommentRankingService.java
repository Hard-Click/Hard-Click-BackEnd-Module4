package com.wanted.backend.domain.ranking.application.service;

import com.wanted.backend.domain.ranking.application.port.RankingListReader;
import com.wanted.backend.domain.ranking.application.query.GetAcceptedCommentRankingQuery;
import com.wanted.backend.domain.ranking.application.usecase.GetAcceptedCommentRankingUseCase;
import com.wanted.backend.domain.ranking.domain.model.RankingList;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
import com.wanted.backend.domain.ranking.domain.policy.RankingPeriodPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAcceptedCommentRankingService implements GetAcceptedCommentRankingUseCase {

    private final RankingListReader rankingListReader;
    private final RankingPeriodPolicy rankingPeriodPolicy;

    @Override
    public AcceptedCommentRankingView handle(GetAcceptedCommentRankingQuery query) {
        RankingPeriod period = rankingPeriodPolicy.resolve(query.period());
        RankingList rankingList = rankingListReader.findByMetricAndPeriod(
                RankingMetric.ACCEPTED_COMMENT,
                period
        );

        return new AcceptedCommentRankingView(
                period.value(),
                rankingList.totalUsers(),
                rankingList.entries().stream()
                        .map(entry -> new AcceptedCommentRankingItem(
                                entry.rank(),
                                entry.memberId(),
                                entry.score()
                        ))
                        .toList()
        );
    }
}
