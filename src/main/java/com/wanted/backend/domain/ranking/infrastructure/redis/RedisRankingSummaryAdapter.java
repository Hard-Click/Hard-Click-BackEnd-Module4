package com.wanted.backend.domain.ranking.infrastructure.redis;

import com.wanted.backend.domain.ranking.application.port.RankingSummaryReader;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
import com.wanted.backend.domain.ranking.domain.model.RankingSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisRankingSummaryAdapter implements RankingSummaryReader {

    private final StringRedisTemplate redisTemplate;
    private final RankingRedisProperties rankingRedisProperties;

    @Override
    public RankingSummary findByMetricAndPeriodAndMemberId(
            RankingMetric metric,
            RankingPeriod period,
            Long memberId
    ) {
        String key = key(metric, period);
        String member = String.valueOf(memberId);
        Long totalUsers = redisTemplate.opsForZSet().zCard(key);
        Long zeroBasedRank = redisTemplate.opsForZSet().reverseRank(key, member);
        if (zeroBasedRank == null) {
            return RankingSummary.notRanked(totalUsers);
        }

        return new RankingSummary(zeroBasedRank + 1, totalUsers == null ? 0L : totalUsers);
    }

    private String key(RankingMetric metric, RankingPeriod period) {
        return rankingRedisProperties.keyPrefix() + ":" + metric.key() + ":" + period.value();
    }
}
