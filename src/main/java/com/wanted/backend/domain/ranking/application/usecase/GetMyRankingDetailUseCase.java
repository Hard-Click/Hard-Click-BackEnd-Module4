package com.wanted.backend.domain.ranking.application.usecase;

import com.wanted.backend.domain.ranking.application.query.GetMyRankingDetailQuery;

public interface GetMyRankingDetailUseCase {

    MyRankingDetailView handle(GetMyRankingDetailQuery query);

    record MyRankingDetailView(
            String metric,
            String period,
            Long rank,
            Long totalUsers,
            Double topPercent
    ) {
    }
}
