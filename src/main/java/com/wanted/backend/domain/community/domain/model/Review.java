package com.wanted.backend.domain.community.domain.model;

import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;

public class Review {

    private final Long id;
    private final Long memberId;
    private final Long courseId;
    private Double rating;
    private String content;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Review(Long id, Long memberId, Long courseId, Double rating, String content,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (memberId == null || courseId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        this.id = id;
        this.memberId = memberId;
        this.courseId = courseId;
        this.rating = rating;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Review create(Long memberId, Long courseId, Double rating, String content) {
        return new Review(null, memberId, courseId, rating, content,
                LocalDateTime.now(), LocalDateTime.now());
    }

    public static Review restore(Long id, Long memberId, Long courseId, Double rating, String content,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Review(id, memberId, courseId, rating, content, createdAt, updatedAt);
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public Long getCourseId() { return courseId; }
    public Double getRating() { return rating; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}