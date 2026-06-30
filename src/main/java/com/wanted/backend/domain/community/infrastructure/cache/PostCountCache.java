package com.wanted.backend.domain.community.infrastructure.cache;

import com.wanted.backend.domain.community.domain.model.BoardType;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

// 게시글 목록 페이지네이션용 전체 건수 COUNT(*)를 캐싱한다.
// 검색(keyword 있음)은 결과 조합이 사실상 무한해 캐시 적중률이 낮으므로 캐싱하지 않고
// 매번 직접 COUNT한다(목록 둘러보기 — keyword 없음 — 가 압도적으로 빈번한 케이스).
//
// @Cacheable 대신 프로그래밍 방식으로 직접 get/put 하는 이유: Redis 기반 RedisCacheManager는
// Caffeine과 달리 히트/미스 통계를 Micrometer에 자동으로 안 보내준다. 그라파나에 캐시 효율을
// 보여주기 위해 히트/미스를 직접 카운터로 기록한다.
@Component
public class PostCountCache {

    private static final String CACHE_NAME = "postCount:v1";

    private final PostRepository postRepository;
    private final CacheManager cacheManager;
    private final Counter cacheHits;
    private final Counter cacheMisses;

    public PostCountCache(PostRepository postRepository, CacheManager cacheManager, MeterRegistry meterRegistry) {
        this.postRepository = postRepository;
        this.cacheManager = cacheManager;
        this.cacheHits = Counter.builder("post.count.cache")
                .tag("result", "hit")
                .register(meterRegistry);
        this.cacheMisses = Counter.builder("post.count.cache")
                .tag("result", "miss")
                .register(meterRegistry);
    }

    public int count(BoardType boardType, String keyword) {
        boolean cacheable = keyword == null || keyword.isBlank();
        if (!cacheable) {
            return load(boardType, keyword);
        }

        Cache cache = cacheManager.getCache(CACHE_NAME);
        String key = boardType != null ? boardType.name() : "ALL";

        Integer cached = cache.get(key, Integer.class);
        if (cached != null) {
            cacheHits.increment();
            return cached;
        }

        cacheMisses.increment();
        int value = load(boardType, keyword);
        cache.put(key, value);
        return value;
    }

    private int load(BoardType boardType, String keyword) {
        return boardType != null
                ? postRepository.countByBoardType(boardType, keyword)
                : postRepository.countAll(keyword);
    }
}
