package com.wanted.backend.domain.ranking.application.usecase;

import com.wanted.backend.domain.ranking.application.query.GetLessonRankingQuery;

import java.util.List;

public interface GetLessonRankingUseCase {

    LessonRankingView handle(GetLessonRankingQuery query);

    record LessonRankingView(
            String period,
            Long totalUsers,
            List<LessonRankingItem> rankings
    ) {
    }

    record LessonRankingItem(
            Long rank,
            Long memberId,
            Long watchedLessonCount
    ) {
    }
}
