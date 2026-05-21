package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.Review;
import com.wanted.backend.domain.community.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryAdapter implements ReviewRepository {

    private final ReviewJpaRepository reviewJpaRepository;

    @Override
    public Review save(Review review) {
        return reviewJpaRepository.save(ReviewJpaEntity.from(review)).toDomain();
    }

    @Override
    public Optional<Review> findByCourseIdAndMemberId(Long courseId, Long memberId) {
        return reviewJpaRepository.findByCourseIdAndMemberId(courseId, memberId)
                .map(ReviewJpaEntity::toDomain);
    }
}