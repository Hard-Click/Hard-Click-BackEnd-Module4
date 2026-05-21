package com.wanted.backend.domain.community.domain.repository;

import com.wanted.backend.domain.community.domain.model.Review;

public interface ReviewRepository {

    Review save(Review review);

    boolean existsByCourseIdAndMemberId(Long courseId, Long memberId);
}