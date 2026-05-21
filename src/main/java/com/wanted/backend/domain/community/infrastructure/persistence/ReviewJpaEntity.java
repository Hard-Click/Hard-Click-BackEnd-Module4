package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.Review;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class ReviewJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Double rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected ReviewJpaEntity() {}

    public static ReviewJpaEntity from(Review review) {
        ReviewJpaEntity entity = new ReviewJpaEntity();
        entity.memberId = review.getMemberId();
        entity.courseId = review.getCourseId();
        entity.rating = review.getRating();
        entity.content = review.getContent();
        entity.createdAt = LocalDateTime.now();
        entity.updatedAt = LocalDateTime.now();
        return entity;
    }

    public Review toDomain() {
        return Review.restore(
                reviewId, memberId, courseId, rating, content, createdAt, updatedAt
        );
    }
}