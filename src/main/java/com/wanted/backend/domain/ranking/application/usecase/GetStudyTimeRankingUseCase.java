package com.wanted.backend.domain.ranking.application.usecase;

import com.wanted.backend.domain.ranking.application.query.GetStudyTimeRankingQuery;

import java.util.List;

public interface GetStudyTimeRankingUseCase {

    StudyTimeRankingView handle(GetStudyTimeRankingQuery query);

    record StudyTimeRankingView(
            String period,
            Long totalUsers,
            List<StudyTimeRankingItem> rankings
    ) {
    }

    record StudyTimeRankingItem(
            Long rank,
            Long memberId,
            String memberName,
            Long studySeconds,
            Integer currentStreakDays
    ) {
    }
}
