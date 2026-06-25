package com.wanted.backend.domain.ranking.infrastructure.redis;

import com.wanted.backend.domain.ranking.application.port.RankingDetailReader;
import com.wanted.backend.domain.ranking.application.port.RankingListReader;
import com.wanted.backend.domain.ranking.application.port.RankingScoreWriter;
import com.wanted.backend.domain.ranking.domain.model.RankingDetail;
import com.wanted.backend.domain.ranking.domain.model.RankingEntry;
import com.wanted.backend.domain.ranking.domain.model.RankingList;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RedisRankingAdapter implements RankingDetailReader, RankingListReader, RankingScoreWriter {

    private final StringRedisTemplate redisTemplate;
    private final RankingRedisProperties rankingRedisProperties;

    @Override
    public RankingDetail findByMetricAndPeriodAndMemberId(
            RankingMetric metric,
            RankingPeriod period,
            Long memberId
    ) {
        String key = key(metric, period);
        String member = String.valueOf(memberId);
        Long totalUsers = redisTemplate.opsForZSet().zCard(key);
        Long zeroBasedRank = redisTemplate.opsForZSet().reverseRank(key, member);
        if (zeroBasedRank == null) {
            return RankingDetail.notRanked(totalUsers);
        }

        return new RankingDetail(zeroBasedRank + 1, totalUsers == null ? 0L : totalUsers);
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

    @Override
    public void incrementScore(
            RankingMetric metric,
            RankingPeriod period,
            Long memberId,
            long scoreDelta
    ) {
        if (scoreDelta <= 0) {
            return;
        }

        redisTemplate.opsForZSet().incrementScore(
                key(metric, period),
                String.valueOf(memberId),
                scoreDelta
        );
    }

    @Override
    public void replaceScores(
            RankingMetric metric,
            RankingPeriod period,
            Map<Long, Long> scores
    ) {
        String key = key(metric, period);
        Set<ZSetOperations.TypedTuple<String>> tuples = scores == null
                ? Set.of()
                : scores.entrySet().stream()
                        .filter(entry -> entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0)
                        .map(entry -> new DefaultTypedTuple<>(
                                String.valueOf(entry.getKey()),
                                entry.getValue().doubleValue()
                        ))
                        .collect(Collectors.toSet());

        if (tuples.isEmpty()) {
            redisTemplate.delete(key);
            return;
        }

        // 임시 키에 적재 후 RENAME으로 교체해 조회 공백 및 add 실패 시 기존 랭킹 소실을 방지한다.
        String tempKey = key + ":rebuild:" + UUID.randomUUID();
        redisTemplate.opsForZSet().add(tempKey, tuples);
        redisTemplate.rename(tempKey, key);
    }

    private String key(RankingMetric metric, RankingPeriod period) {
        return rankingRedisProperties.keyPrefix() + ":" + metric.key() + ":" + period.value();
    }
}
