package com.wanted.backend.domain.ranking.application.service;

import com.wanted.backend.domain.ranking.application.port.RankingListReader;
import com.wanted.backend.domain.ranking.application.query.GetLessonRankingQuery;
import com.wanted.backend.domain.ranking.application.usecase.GetLessonRankingUseCase;
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
public class GetLessonRankingService implements GetLessonRankingUseCase {

    private final RankingListReader rankingListReader;
    private final RankingPeriodPolicy rankingPeriodPolicy;

    @Override
    public LessonRankingView handle(GetLessonRankingQuery query) {
        RankingPeriod period = rankingPeriodPolicy.resolve(query.period());
        RankingList rankingList = rankingListReader.findByMetricAndPeriod(
                RankingMetric.LESSON,
                period
        );

        return new LessonRankingView(
                period.value(),
                rankingList.totalUsers(),
                rankingList.entries().stream()
                        .map(entry -> new LessonRankingItem(
                                entry.rank(),
                                entry.memberId(),
                                entry.score()
                        ))
                        .toList()
        );
    }
}
