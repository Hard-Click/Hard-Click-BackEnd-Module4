package com.wanted.backend.domain.community.application.usecase;

import com.wanted.backend.domain.community.application.result.ReviewListResult;
import com.wanted.backend.domain.community.domain.model.ReviewSortType;

public interface ReviewQueryUseCase {
    ReviewListResult handle(Long courseId, Long memberId, ReviewSortType sort, int page);
}