package com.wanted.backend.domain.ranking.infrastructure.redis;

import com.wanted.backend.domain.ranking.domain.model.RankingMetric;
import com.wanted.backend.domain.ranking.domain.model.RankingPeriod;
import com.wanted.backend.domain.ranking.domain.model.RankingSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisRankingSummaryAdapterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    private RedisRankingSummaryAdapter adapter;

    @BeforeEach
    void setUp() {
        RankingRedisProperties properties = new RankingRedisProperties();
        properties.setKeyPrefix("ranking");
        adapter = new RedisRankingSummaryAdapter(redisTemplate, properties);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    void readsRankAndTotalUsersFromRedisSortedSet() {
        when(zSetOperations.zCard("ranking:study-time:monthly")).thenReturn(200L);
        when(zSetOperations.reverseRank("ranking:study-time:monthly", "1")).thenReturn(11L);

        RankingSummary result = adapter.findByMetricAndPeriodAndMemberId(
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

        RankingSummary result = adapter.findByMetricAndPeriodAndMemberId(
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

        RankingSummary result = adapter.findByMetricAndPeriodAndMemberId(
                RankingMetric.ACCEPTED_COMMENT,
                RankingPeriod.MONTHLY,
                1L
        );

        assertThat(result.rank()).isNull();
        assertThat(result.totalUsers()).isZero();
    }
}
