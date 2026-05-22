package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.Review;
import com.wanted.backend.domain.community.domain.model.ReviewSortType;
import com.wanted.backend.domain.community.domain.repository.ReviewRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewRepositoryAdapter implements ReviewRepository {

    private final SpringDataReviewRepository repository;

    @PersistenceContext
    private EntityManager em;

    public ReviewRepositoryAdapter(SpringDataReviewRepository repository) {
        this.repository = repository;
    }


    @Override
    public boolean existsByCourseIdAndMemberId(Long courseId, Long memberId) {
        return repository.existsByCourseIdAndMemberId(courseId, memberId);
    }

    @Override
    public Optional<Review> findByCourseIdAndMemberId(Long courseId, Long memberId) {
        return repository.findByCourseIdAndMemberId(courseId, memberId)
                .map(this::toDomain);
    }

    @Override
    public List<Review> findByCourseIdExcludeMember(Long courseId, Long memberId,
                                                    ReviewSortType sort, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, toSort(sort));
        return repository.findByCourseIdAndMemberIdNot(courseId, memberId, pageable)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Review> findByCourseId(Long courseId, ReviewSortType sort, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, toSort(sort));
        return repository.findByCourseId(courseId, pageable)
                .stream()
                .map(this::toDomain)
                .toList();
    }
    //집계 쿼리 직접 사용
    @Override
    public List<ReviewRepository.RatingCount> countGroupByRating(Long courseId) {

        List<Object[]> rows = em.createQuery("""
                        SELECT r.rating, COUNT(r)
                        FROM ReviewJpaEntity r
                        WHERE r.courseId = :courseId
                        GROUP BY r.rating
                        ORDER BY r.rating DESC
                        """, Object[].class)
                .setParameter("courseId", courseId)
                .getResultList();

        return rows.stream()
                .map(row -> new ReviewRepository.RatingCount(
                        (Double) row[0],
                        (Long) row[1]
                ))
                .toList();
    }

    @Override
    public int countByCourseId(Long courseId) {
        return repository.countByCourseId(courseId);
    }

    //집계 쿼리 직접 사용
    @Override
    public Double avgRatingByCourseId(Long courseId) {
        Double avg = em.createQuery("""
                        SELECT AVG(r.rating)
                        FROM ReviewJpaEntity r
                        WHERE r.courseId = :courseId
                        """, Double.class)
                .setParameter("courseId", courseId)
                .getSingleResult();
        return avg != null ? avg : 0.0;
    }

    private Sort toSort(ReviewSortType sort) {
        return switch (sort) {
            case rating -> Sort.by(Sort.Direction.DESC, "rating");
            case latest -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    @Override
    public Optional<Review> findById(Long reviewId) {
        return repository.findById(reviewId)
                .map(this::toDomain);
    }

    @Override
    public Review save(Review review) {

        if (review.getId() != null) {
            ReviewJpaEntity entity = repository.findById(review.getId())
                    .orElseThrow();
            entity.update(review.getRating(), review.getContent(), review.getUpdatedAt());
            return toDomain(repository.save(entity));
        }

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