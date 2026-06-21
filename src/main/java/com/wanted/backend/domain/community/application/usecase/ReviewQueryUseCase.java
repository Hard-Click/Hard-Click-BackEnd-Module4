package com.wanted.backend.domain.community.application.usecase;

import com.wanted.backend.domain.community.domain.model.ReviewSortType;
import com.wanted.backend.domain.community.presentation.response.ReviewListResponse;

public interface ReviewQueryUseCase {

    ReviewListResponse handle(Long courseId, Long memberId, ReviewSortType sort, int page);

}