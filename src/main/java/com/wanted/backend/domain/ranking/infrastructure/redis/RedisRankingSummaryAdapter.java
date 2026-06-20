package com.wanted.backend.domain.ranking.infrastructure.redis;

import com.wanted.backend.domain.ranking.application.port.RankingSummaryReader;
import com.wanted.backend.domain.ranking.application.port.RankingListReader;
import com.wanted.backend.domain.ranking.domain.model.RankingEntry;
import com.wanted.backend.domain.ranking.domain.model.RankingList;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
import com.wanted.backend.domain.ranking.domain.model.RankingSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RedisRankingSummaryAdapter implements RankingSummaryReader, RankingListReader {

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

    @Override
    public RankingList findByMetricAndPeriod(
            RankingMetric metric,
            RankingPeriod period
    ) {
        String key = key(metric, period);
        Long totalUsers = redisTemplate.opsForZSet().zCard(key);
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, 0, rankingRedisProperties.defaultLimit() - 1);
        if (tuples == null || tuples.isEmpty()) {
            return RankingList.empty(totalUsers);
        }

        List<RankingEntry> entries = new ArrayList<>();
        long rank = 1L;
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            if (tuple == null || tuple.getValue() == null || tuple.getValue().isBlank() || tuple.getScore() == null) {
                continue;
            }

            Long memberId;
            try {
                memberId = Long.valueOf(tuple.getValue());
            } catch (NumberFormatException exception) {
                continue;
            }

            entries.add(new RankingEntry(
                    rank,
                    memberId,
                    Math.round(tuple.getScore())
            ));
            rank++;
        }

        return new RankingList(totalUsers == null ? 0L : totalUsers, entries);
    }

    private String key(RankingMetric metric, RankingPeriod period) {
        return rankingRedisProperties.keyPrefix() + ":" + metric.key() + ":" + period.value();
    }
}
