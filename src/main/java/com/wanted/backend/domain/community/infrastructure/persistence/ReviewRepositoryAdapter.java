package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.Review;
import com.wanted.backend.domain.community.domain.repository.ReviewRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ReviewRepositoryAdapter implements ReviewRepository {

    private final ReviewJpaRepository repository;

    public ReviewRepositoryAdapter(ReviewJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Review save(Review review) {
        ReviewJpaEntity entity = new ReviewJpaEntity(
                review.getMemberId(),
                review.getCourseId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );

        return toDomain(repository.save(entity));
    }

    @Override
    public boolean existsByCourseIdAndMemberId(Long courseId, Long memberId) {
        return repository.existsByCourseIdAndMemberId(courseId, memberId);
    }

    private Review toDomain(ReviewJpaEntity entity) {
        return Review.restore(
                entity.getId(),
                entity.getMemberId(),
                entity.getCourseId(),
                entity.getRating(),
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}