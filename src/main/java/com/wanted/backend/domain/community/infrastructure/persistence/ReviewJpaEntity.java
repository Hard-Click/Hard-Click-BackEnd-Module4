package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.ReviewStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_reviews_course_id", columnList = "course_id")
})
@Getter
public class ReviewJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatus status = ReviewStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ReviewJpaEntity() {}

    public ReviewJpaEntity(Long memberId, Long courseId, Integer rating, String content,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.memberId = memberId;
        this.courseId = courseId;
        this.rating = rating;
        this.content = content;
        this.status = ReviewStatus.ACTIVE;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void update(Integer rating, String content, ReviewStatus status, LocalDateTime updatedAt) {
        this.rating = rating;
        this.content = content;
        this.status = status == null ? this.status : status;
        this.updatedAt = updatedAt;
    }

    public void update(Integer rating, String content, LocalDateTime updatedAt) {
        update(rating, content, this.status, updatedAt);
    }
}
