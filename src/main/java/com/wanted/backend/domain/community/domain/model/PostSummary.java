package com.wanted.backend.domain.community.domain.model;

import java.time.LocalDateTime;

public class PostSummary {

    private final Long id;
    private final BoardType boardType;
    private final String title;
    private final String authorName;
    private final LocalDateTime createdAt;
    private final int viewCount;
    private final long commentCount;

    public PostSummary(Long id, BoardType boardType, String title,
                       String authorName, LocalDateTime createdAt, int viewCount, long commentCount) {
        this.id = id;
        this.boardType = boardType;
        this.title = title;
        this.authorName = authorName;
        this.createdAt = createdAt;
        this.viewCount = viewCount;
        this.commentCount = commentCount;
    }

    public Long getId() { return id; }
    public BoardType getBoardType() { return boardType; }
    public String getTitle() { return title; }
    public String getAuthorName() { return authorName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getViewCount() { return viewCount; }
    public long getCommentCount() { return commentCount; }
}
