package com.wanted.backend.domain.community.domain.repository;

import com.wanted.backend.domain.community.domain.model.Review;

import java.util.Optional;

public interface ReviewRepository {

    Review save(Review review);

    Optional<Review> findByCourseIdAndMemberId(Long courseId, Long memberId);
}