package com.wanted.backend.domain.ranking.application.port;

import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
import com.wanted.backend.domain.ranking.domain.model.RankingSummary;

public interface RankingSummaryReader {

    RankingSummary findByMetricAndPeriodAndMemberId(
            RankingMetric metric,
            RankingPeriod period,
            Long memberId
    );
}
