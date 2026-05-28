package com.wanted.backend.domain.notice.domain.model;

import java.time.LocalDateTime;

public class Notice {

    private Long id;
    private Long authorId;
    private Long courseId;
    private String title;
    private String content;
    private boolean isPinned;
    private String type;
    private NoticeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Notice(Long id, Long authorId, Long courseId, String title, String content,
                   boolean isPinned, String type, NoticeStatus status,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.authorId = authorId;
        this.courseId = courseId;
        this.title = title;
        this.content = content;
        this.isPinned = isPinned;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Notice create(Long authorId, Long courseId, String title,
                                String content, boolean isPinned) {
        return new Notice(null, authorId, courseId, title, content,
                isPinned, "COURSE", NoticeStatus.PUBLISHED,
                LocalDateTime.now(), LocalDateTime.now());
    }

    public static Notice restore(Long id, Long authorId, Long courseId, String title,
                                 String content, boolean isPinned, String type,
                                 NoticeStatus status, LocalDateTime createdAt,
                                 LocalDateTime updatedAt) {
        return new Notice(id, authorId, courseId, title, content,
                isPinned, type, status, createdAt, updatedAt);
    }

    public Long getId() { return id; }
    public Long getAuthorId() { return authorId; }
    public Long getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public boolean isPinned() { return isPinned; }
    public String getType() { return type; }
    public NoticeStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}