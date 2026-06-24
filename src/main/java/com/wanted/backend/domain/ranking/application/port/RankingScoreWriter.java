package com.wanted.backend.domain.ranking.application.port;

import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;

import java.util.Map;

public interface RankingScoreWriter {

    void incrementScore(
            RankingMetric metric,
            RankingPeriod period,
            Long memberId,
            long scoreDelta
    );

    void replaceScores(
            RankingMetric metric,
            RankingPeriod period,
            Map<Long, Long> scores
    );
}
