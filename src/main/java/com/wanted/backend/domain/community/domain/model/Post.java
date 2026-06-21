package com.wanted.backend.domain.community.domain.model;

import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;

public class Post {

    private Long id;
    private Long authorId;
    private BoardType boardType;
    private String subject;
    private String title;
    private String content;
    private int viewCount;
    private PostStatus status;
    private boolean isAccepted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Post(Long id, Long authorId, BoardType boardType, String subject,
                 String title, String content, int viewCount, PostStatus status, boolean isAccepted,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.authorId = authorId;
        this.boardType = boardType;
        this.subject = subject;
        this.title = title;
        this.content = content;
        this.viewCount = viewCount;
        this.status = status;
        this.isAccepted = isAccepted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Post create(Long authorId, BoardType boardType, String subject,
                              String title, String content, int fileCount) {
        if (boardType == BoardType.QUESTION && subject == null) {
            throw new BusinessException(ErrorCode.SUBJECT_REQUIRED);
        }
        if (fileCount > 2) {
            throw new BusinessException(ErrorCode.FILE_COUNT_EXCEEDED);
        }
        return new Post(null, authorId, boardType, subject, title, content,
                0, PostStatus.ACTIVE, false, LocalDateTime.now(), LocalDateTime.now());
    }

    public static Post restore(Long id, Long authorId, BoardType boardType, String subject,
                               String title, String content, int viewCount, PostStatus status, boolean isAccepted,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Post(id, authorId, boardType, subject, title, content,
                viewCount, status == null ? PostStatus.ACTIVE : status, isAccepted, createdAt, updatedAt);
    }

    public static Post restore(Long id, Long authorId, BoardType boardType, String subject,
                               String title, String content, int viewCount, boolean isAccepted,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        return restore(id, authorId, boardType, subject, title, content,
                viewCount, PostStatus.ACTIVE, isAccepted, createdAt, updatedAt);
    }

    public void increaseViewCount() {
        this.viewCount++;
        this.updatedAt = LocalDateTime.now();
    }

    public void validateDeletable(Long memberId) {
        if (!this.authorId.equals(memberId)) {
            throw new BusinessException(ErrorCode.POST_NOT_AUTHORIZED);
        }
        if (this.isAccepted) {
            throw new BusinessException(ErrorCode.POST_ACCEPTED_CANNOT_DELETE);
        }
    }

    public void validateUpdatable(Long memberId) {
        if (!this.authorId.equals(memberId)) {
            throw new BusinessException(ErrorCode.POST_NOT_AUTHORIZED);
        }
        if (this.isAccepted) {
            throw new BusinessException(ErrorCode.POST_ACCEPTED_CANNOT_MODIFY);
        }
    }

    public void update(String subject, String title, String content) {
        if (boardType == BoardType.QUESTION && subject == null) {
            throw new BusinessException(ErrorCode.SUBJECT_REQUIRED);
        }
        this.subject = subject;
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void softDeleteByAdmin(LocalDateTime now) {
        this.status = PostStatus.ADMIN_DELETED;
        this.updatedAt = now;
    }

    public void validateFileCount(int fileCount) {
        if (fileCount > 2) {
            throw new BusinessException(ErrorCode.FILE_COUNT_EXCEEDED);
        }
    }

    public void markAsAccepted() {
        this.isAccepted = true;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOwner(Long memberId) {
        if (memberId == null) return false;
        return this.authorId.equals(memberId);
    }

    public boolean isAdminDeleted() {
        return this.status == PostStatus.ADMIN_DELETED;
    }

    public Long getId() { return id; }
    public Long getAuthorId() { return authorId; }
    public BoardType getBoardType() { return boardType; }
    public String getSubject() { return subject; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public int getViewCount() { return viewCount; }
    public PostStatus getStatus() { return status; }
    public boolean isAccepted() { return isAccepted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
