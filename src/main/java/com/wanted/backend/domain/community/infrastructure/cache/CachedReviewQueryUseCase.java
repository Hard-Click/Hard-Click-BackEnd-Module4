package com.wanted.backend.domain.community.infrastructure.cache;

import com.wanted.backend.domain.community.application.result.ReviewListResult;
import com.wanted.backend.domain.community.application.usecase.ReviewQueryUseCase;
import com.wanted.backend.domain.community.domain.model.ReviewSortType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class CachedReviewQueryUseCase implements ReviewQueryUseCase {

    private final ReviewQueryUseCase delegate;

    public CachedReviewQueryUseCase(
            @Qualifier("reviewQueryService") ReviewQueryUseCase delegate
    ) {
        this.delegate = delegate;
    }

    @Override
    @Cacheable(
            cacheNames = "reviews:guest",
            key = "#courseId + ':' + #sort.name() + ':' + #page",
            condition = "#memberId == -1"
    )
    public ReviewListResult handle(Long courseId, Long memberId, ReviewSortType sort, int page) {
        return delegate.handle(courseId, memberId, sort, page);
    }
}