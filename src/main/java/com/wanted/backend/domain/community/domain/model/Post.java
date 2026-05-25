package com.wanted.backend.domain.community.domain.model;

import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;

public class Post {

    private Long id;
    private Long authorId;
    private BoardType boardType;
    private Long subjectId;
    private String title;
    private String content;
    private int viewCount;
    private boolean isAccepted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Post(Long id, Long authorId, BoardType boardType, Long subjectId,
                 String title, String content, int viewCount, boolean isAccepted,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.authorId = authorId;
        this.boardType = boardType;
        this.subjectId = subjectId;
        this.title = title;
        this.content = content;
        this.viewCount = viewCount;
        this.isAccepted = isAccepted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Post create(Long authorId, BoardType boardType, Long subjectId,
                              String title, String content, int fileCount) {
        if (boardType == BoardType.QUESTION && subjectId == null) {
            throw new BusinessException(ErrorCode.SUBJECT_REQUIRED);
        }
        if (fileCount > 2) {
            throw new BusinessException(ErrorCode.FILE_COUNT_EXCEEDED);
        }
        return new Post(null, authorId, boardType, subjectId, title, content,
                0, false, LocalDateTime.now(), LocalDateTime.now());
    }

    public static Post restore(Long id, Long authorId, BoardType boardType, Long subjectId,
                               String title, String content, int viewCount, boolean isAccepted,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Post(id, authorId, boardType, subjectId, title, content,
                viewCount, isAccepted, createdAt, updatedAt);
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

    public Long getId() { return id; }
    public Long getAuthorId() { return authorId; }
    public BoardType getBoardType() { return boardType; }
    public Long getSubjectId() { return subjectId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public int getViewCount() { return viewCount; }
    public boolean isAccepted() { return isAccepted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}