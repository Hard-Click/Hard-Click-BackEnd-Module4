package com.wanted.backend.domain.ranking.application.service;

import com.wanted.backend.domain.ranking.application.port.RankingDetailReader;
import com.wanted.backend.domain.ranking.application.query.GetMyRankingSummaryQuery;
import com.wanted.backend.domain.ranking.application.usecase.GetMyRankingSummaryUseCase;
import com.wanted.backend.domain.ranking.domain.model.RankingDetail;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
import com.wanted.backend.domain.ranking.domain.policy.RankingTopPercentPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyRankingSummaryService implements GetMyRankingSummaryUseCase {

    private final RankingDetailReader rankingDetailReader;
    private final RankingTopPercentPolicy rankingTopPercentPolicy;

    @Override
    public MyRankingSummaryView handle(GetMyRankingSummaryQuery query) {
        validate(query);

        return new MyRankingSummaryView(
                findSummaryItem(query.memberId(), RankingMetric.STUDY_TIME),
                findSummaryItem(query.memberId(), RankingMetric.LESSON),
                findSummaryItem(query.memberId(), RankingMetric.ACCEPTED_COMMENT)
        );
    }

    private RankingSummaryItem findSummaryItem(Long memberId, RankingMetric metric) {
        RankingDetail detail = findRankingDetail(memberId, metric);

        return new RankingSummaryItem(
                detail.rank(),
                detail.totalUsers(),
                rankingTopPercentPolicy.calculate(detail.rank(), detail.totalUsers())
        );
    }

    private RankingDetail findRankingDetail(Long memberId, RankingMetric metric) {
        try {
            return rankingDetailReader.findByMetricAndPeriodAndMemberId(
                    metric,
                    RankingPeriod.MONTHLY,
                    memberId
            );
        } catch (DataAccessException exception) {
            return RankingDetail.empty();
        }
    }

    private void validate(GetMyRankingSummaryQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("요청은 필수입니다.");
        }
        if (query.memberId() == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
    }
}
