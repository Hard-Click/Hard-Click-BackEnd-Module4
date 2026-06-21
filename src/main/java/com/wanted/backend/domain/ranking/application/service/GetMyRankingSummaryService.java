package com.wanted.backend.domain.ranking.application.service;

import com.wanted.backend.domain.ranking.application.port.RankingSummaryReader;
import com.wanted.backend.domain.ranking.application.query.GetMyRankingSummaryQuery;
import com.wanted.backend.domain.ranking.application.usecase.GetMyRankingSummaryUseCase;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
import com.wanted.backend.domain.ranking.domain.model.RankingSummary;
import com.wanted.backend.domain.ranking.domain.policy.RankingMetricPolicy;
import com.wanted.backend.domain.ranking.domain.policy.RankingPeriodPolicy;
import com.wanted.backend.domain.ranking.domain.policy.RankingTopPercentPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyRankingSummaryService implements GetMyRankingSummaryUseCase {

    private final RankingSummaryReader rankingSummaryReader;
    private final RankingMetricPolicy rankingMetricPolicy;
    private final RankingPeriodPolicy rankingPeriodPolicy;
    private final RankingTopPercentPolicy rankingTopPercentPolicy;

    @Override
    public MyRankingSummaryView handle(GetMyRankingSummaryQuery query) {
        validate(query);

        RankingMetric metric = rankingMetricPolicy.resolve(query.metric());
        RankingPeriod period = rankingPeriodPolicy.resolve(query.period());
        RankingSummary summary = rankingSummaryReader.findByMetricAndPeriodAndMemberId(
                metric,
                period,
                query.memberId()
        );

        return new MyRankingSummaryView(
                metric.key(),
                period.value(),
                summary.rank(),
                summary.totalUsers(),
                rankingTopPercentPolicy.calculate(summary.rank(), summary.totalUsers())
        );
    }

    private void validate(GetMyRankingSummaryQuery query) {
        if (query.memberId() == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
    }
}
