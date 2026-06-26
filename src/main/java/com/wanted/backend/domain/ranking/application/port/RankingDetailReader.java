package com.wanted.backend.domain.ranking.application.port;

import com.wanted.backend.domain.ranking.domain.model.RankingDetail;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;

public interface RankingDetailReader {

    RankingDetail findByMetricAndPeriodAndMemberId(
            RankingMetric metric,
            RankingPeriod period,
            Long memberId
    );
}
