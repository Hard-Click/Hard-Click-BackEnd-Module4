package com.wanted.backend.domain.community.domain.model;

import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;

public class Review {

    private final Long id;
    private final Long memberId;
    private final Long courseId;
    private Integer rating;
    private String content;
    private ReviewStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Review(Long id, Long memberId, Long courseId, Integer rating, String content, ReviewStatus status,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (memberId == null || courseId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        this.id = id;
        this.memberId = memberId;
        this.courseId = courseId;
        this.rating = rating;
        this.content = content;
        this.status = status == null ? ReviewStatus.ACTIVE : status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Review create(Long memberId, Long courseId, Integer rating, String content) {
        return new Review(null, memberId, courseId, rating, content, ReviewStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now());
    }

    public static Review restore(Long id, Long memberId, Long courseId, Integer rating, String content,
                                 ReviewStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Review(id, memberId, courseId, rating, content, status, createdAt, updatedAt);
    }

    public boolean isOwner(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public static String maskName(String name) {
        if (name.length() == 1) return name;
        if (name.length() == 2) return name.charAt(0) + "*";
        return name.charAt(0) + "*" + name.charAt(name.length() - 1);
    }

    public void update(Integer rating, String content) {
        if (this.status != ReviewStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_AUTHORIZED);
        }

        this.rating = rating;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public Long getCourseId() { return courseId; }
    public Integer getRating() { return rating; }
    public String getContent() { return content; }
    public ReviewStatus getStatus() { return status; }
    public boolean isAdminDeleted() { return status == ReviewStatus.ADMIN_DELETED; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
