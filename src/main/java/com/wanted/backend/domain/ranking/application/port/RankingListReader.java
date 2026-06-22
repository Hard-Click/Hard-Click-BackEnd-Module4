package com.wanted.backend.domain.ranking.application.port;

import com.wanted.backend.domain.ranking.domain.model.RankingList;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;

public interface RankingListReader {

    RankingList findByMetricAndPeriod(
            RankingMetric metric,
            RankingPeriod period
    );
}
