package com.wanted.backend.domain.ranking.application.usecase;

import com.wanted.backend.domain.ranking.application.query.GetMyRankingSummaryQuery;

public interface GetMyRankingSummaryUseCase {

    MyRankingSummaryView handle(GetMyRankingSummaryQuery query);

    record MyRankingSummaryView(
            RankingSummaryItem studyTime,
            RankingSummaryItem lesson,
            RankingSummaryItem acceptedComment
    ) {
    }

    record RankingSummaryItem(
            Long rank,
            Long totalUsers,
            Double topPercent
    ) {
    }
}
