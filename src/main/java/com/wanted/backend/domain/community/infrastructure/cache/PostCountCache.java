package com.wanted.backend.domain.community.infrastructure.cache;

import com.wanted.backend.domain.community.domain.model.BoardType;
import com.wanted.backend.domain.community.domain.repository.PostRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

// 게시글 목록 페이지네이션용 전체 건수 COUNT(*)를 캐싱한다.
// 검색(keyword 있음)은 결과 조합이 사실상 무한해 캐시 적중률이 낮으므로 캐싱하지 않고
// 매번 직접 COUNT한다(목록 둘러보기 — keyword 없음 — 가 압도적으로 빈번한 케이스).
@Component
public class PostCountCache {

    private final PostRepository postRepository;

    public PostCountCache(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Cacheable(
            cacheNames = "postCount:v1",
            key = "#boardType != null ? #boardType.name() : 'ALL'",
            condition = "#keyword == null || #keyword.isBlank()"
    )
    public int count(BoardType boardType, String keyword) {
        return boardType != null
                ? postRepository.countByBoardType(boardType, keyword)
                : postRepository.countAll(keyword);
    }
}
