package com.wanted.backend.domain.notice.infrastructure.persistence;

import com.wanted.backend.domain.notice.domain.model.NoticeStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;


@Entity
@Table(name = "notices")
@Getter
public class NoticeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned;

    @Column(nullable = false, length = 20)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoticeStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected NoticeJpaEntity() {}

    public void update(String title, String content, boolean isPinned,
                       LocalDateTime updatedAt) {
        this.title = title;
        this.content = content;
        this.isPinned = isPinned;
        this.updatedAt = updatedAt;
    }

    public NoticeJpaEntity(Long authorId, Long courseId, String title, String content,
                           boolean isPinned, String type, NoticeStatus status,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
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
}