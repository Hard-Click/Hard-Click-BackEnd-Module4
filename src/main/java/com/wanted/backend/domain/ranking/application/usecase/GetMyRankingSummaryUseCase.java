package com.wanted.backend.domain.ranking.application.usecase;

import com.wanted.backend.domain.ranking.application.query.GetMyRankingSummaryQuery;

public interface GetMyRankingSummaryUseCase {

    MyRankingSummaryView handle(GetMyRankingSummaryQuery query);

    record MyRankingSummaryView(
            String metric,
            String period,
            Long rank,
            Long totalUsers,
            Double topPercent
    ) {
    }
}
