package com.wanted.backend.domain.community.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
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
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}