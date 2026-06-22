package com.wanted.backend.domain.ranking.infrastructure.redis;

import com.wanted.backend.domain.ranking.domain.model.RankingDetail;
import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.DefaultTypedTuple;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisRankingAdapterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    private RedisRankingAdapter adapter;

    @BeforeEach
    void setUp() {
        RankingRedisProperties properties = new RankingRedisProperties();
        properties.setKeyPrefix("ranking");
        properties.setDefaultLimit(100);
        adapter = new RedisRankingAdapter(redisTemplate, properties);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    void readsRankAndTotalUsersFromRedisSortedSet() {
        when(zSetOperations.zCard("ranking:study-time:monthly")).thenReturn(200L);
        when(zSetOperations.reverseRank("ranking:study-time:monthly", "1")).thenReturn(11L);

        RankingDetail result = adapter.findByMetricAndPeriodAndMemberId(
                RankingMetric.STUDY_TIME,
                RankingPeriod.MONTHLY,
                1L
        );

        assertThat(result.rank()).isEqualTo(12L);
        assertThat(result.totalUsers()).isEqualTo(200L);
    }

    @Test
    void returnsDefaultSummaryWhenRankDoesNotExist() {
        when(zSetOperations.zCard("ranking:lessons:weekly")).thenReturn(150L);
        when(zSetOperations.reverseRank("ranking:lessons:weekly", "1")).thenReturn(null);

        RankingDetail result = adapter.findByMetricAndPeriodAndMemberId(
                RankingMetric.LESSON,
                RankingPeriod.WEEKLY,
                1L
        );

        assertThat(result.rank()).isNull();
        assertThat(result.totalUsers()).isEqualTo(150L);
        verify(zSetOperations).reverseRank("ranking:lessons:weekly", "1");
    }

    @Test
    void returnsZeroTotalUsersWhenSortedSetDoesNotExist() {
        when(zSetOperations.zCard("ranking:accepted-comments:monthly")).thenReturn(null);
        when(zSetOperations.reverseRank("ranking:accepted-comments:monthly", "1")).thenReturn(null);

        RankingDetail result = adapter.findByMetricAndPeriodAndMemberId(
                RankingMetric.ACCEPTED_COMMENT,
                RankingPeriod.MONTHLY,
                1L
        );

        assertThat(result.rank()).isNull();
        assertThat(result.totalUsers()).isZero();
    }

    @Test
    void readsRankingListFromRedisSortedSet() {
        Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
        tuples.add(new DefaultTypedTuple<>("1", 7200.0));
        tuples.add(new DefaultTypedTuple<>("2", 3600.0));
        when(zSetOperations.zCard("ranking:study-time:daily")).thenReturn(2L);
        when(zSetOperations.reverseRangeWithScores("ranking:study-time:daily", 0, 99))
                .thenReturn(tuples);

        var result = adapter.findByMetricAndPeriod(
                RankingMetric.STUDY_TIME,
                RankingPeriod.DAILY
        );

        assertThat(result.totalUsers()).isEqualTo(2L);
        assertThat(result.entries()).hasSize(2);
        assertThat(result.entries().get(0).rank()).isEqualTo(1L);
        assertThat(result.entries().get(0).memberId()).isEqualTo(1L);
        assertThat(result.entries().get(0).score()).isEqualTo(7200L);
        assertThat(result.entries().get(1).rank()).isEqualTo(2L);
        assertThat(result.entries().get(1).memberId()).isEqualTo(2L);
        assertThat(result.entries().get(1).score()).isEqualTo(3600L);
    }

    @Test
    void skipsInvalidRankingEntriesFromRedisSortedSet() {
        Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
        tuples.add(new DefaultTypedTuple<>("invalid-member", 9000.0));
        tuples.add(new DefaultTypedTuple<>(" ", 7200.0));
        tuples.add(new DefaultTypedTuple<>("2", 3600.0));
        when(zSetOperations.zCard("ranking:study-time:daily")).thenReturn(3L);
        when(zSetOperations.reverseRangeWithScores("ranking:study-time:daily", 0, 99))
                .thenReturn(tuples);

        var result = adapter.findByMetricAndPeriod(
                RankingMetric.STUDY_TIME,
                RankingPeriod.DAILY
        );

        assertThat(result.totalUsers()).isEqualTo(3L);
        assertThat(result.entries()).hasSize(1);
        assertThat(result.entries().get(0).rank()).isEqualTo(1L);
        assertThat(result.entries().get(0).memberId()).isEqualTo(2L);
        assertThat(result.entries().get(0).score()).isEqualTo(3600L);
    }

    @Test
    void returnsEmptyRankingListWhenDataDoesNotExist() {
        when(zSetOperations.zCard("ranking:study-time:weekly")).thenReturn(0L);
        when(zSetOperations.reverseRangeWithScores("ranking:study-time:weekly", 0, 99))
                .thenReturn(Set.of());

        var result = adapter.findByMetricAndPeriod(
                RankingMetric.STUDY_TIME,
                RankingPeriod.WEEKLY
        );

        assertThat(result.totalUsers()).isZero();
        assertThat(result.entries()).isEmpty();
    }
}
