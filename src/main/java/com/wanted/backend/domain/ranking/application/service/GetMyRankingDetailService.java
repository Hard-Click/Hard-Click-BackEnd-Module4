package com.wanted.backend.domain.ranking.application.service;

import com.wanted.backend.domain.ranking.application.port.RankingDetailReader;
import com.wanted.backend.domain.ranking.application.query.GetMyRankingDetailQuery;
import com.wanted.backend.domain.ranking.application.usecase.GetMyRankingDetailUseCase;
import com.wanted.backend.domain.ranking.domain.model.RankingDetail;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
import com.wanted.backend.domain.ranking.domain.policy.RankingMetricPolicy;
import com.wanted.backend.domain.ranking.domain.policy.RankingPeriodPolicy;
import com.wanted.backend.domain.ranking.domain.policy.RankingTopPercentPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyRankingDetailService implements GetMyRankingDetailUseCase {

    private final RankingDetailReader rankingDetailReader;
    private final RankingMetricPolicy rankingMetricPolicy;
    private final RankingPeriodPolicy rankingPeriodPolicy;
    private final RankingTopPercentPolicy rankingTopPercentPolicy;

    @Override
    public MyRankingDetailView handle(GetMyRankingDetailQuery query) {
        validate(query);

        RankingMetric metric = rankingMetricPolicy.resolve(query.metric());
        RankingPeriod period = rankingPeriodPolicy.resolve(query.period());
        RankingDetail detail = rankingDetailReader.findByMetricAndPeriodAndMemberId(
                metric,
                period,
                query.memberId()
        );

        return new MyRankingDetailView(
                metric.key(),
                period.value(),
                detail.rank(),
                detail.totalUsers(),
                rankingTopPercentPolicy.calculate(detail.rank(), detail.totalUsers())
        );
    }

    private void validate(GetMyRankingDetailQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("요청은 필수입니다.");
        }
        if (query.memberId() == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
    }
}
