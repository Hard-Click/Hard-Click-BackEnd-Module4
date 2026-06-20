package com.wanted.backend.domain.ranking.application.service;

import com.wanted.backend.domain.ranking.application.port.RankingListReader;
import com.wanted.backend.domain.ranking.application.query.GetStudyTimeRankingQuery;
import com.wanted.backend.domain.ranking.application.usecase.GetStudyTimeRankingUseCase;
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
public class GetStudyTimeRankingService implements GetStudyTimeRankingUseCase {

    private final RankingListReader rankingListReader;
    private final RankingPeriodPolicy rankingPeriodPolicy;

    @Override
    public StudyTimeRankingView handle(GetStudyTimeRankingQuery query) {
        RankingPeriod period = rankingPeriodPolicy.resolve(query.period());
        RankingList rankingList = rankingListReader.findByMetricAndPeriod(
                RankingMetric.STUDY_TIME,
                period
        );

        return new StudyTimeRankingView(
                period.value(),
                rankingList.totalUsers(),
                rankingList.entries().stream()
                        .map(entry -> new StudyTimeRankingItem(
                                entry.rank(),
                                entry.memberId(),
                                entry.score()
                        ))
                        .toList()
        );
    }
}
