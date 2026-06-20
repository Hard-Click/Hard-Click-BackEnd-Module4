package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.BoardType;
import com.wanted.backend.domain.community.domain.model.PostStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
public class PostJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_type", nullable = false)
    private BoardType boardType;

    @Column(name = "subject_id")
    private Long subjectId;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PostStatus status;

    @Column(name = "is_accepted", nullable = false)
    private boolean isAccepted;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PostJpaEntity() {}

    public void updateViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public PostJpaEntity(Long authorId, BoardType boardType, Long subjectId,
                         String title, String content, int viewCount,
                         boolean isAccepted, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.authorId = authorId;
        this.boardType = boardType;
        this.subjectId = subjectId;
        this.title = title;
        this.content = content;
        this.viewCount = viewCount;
        this.status = PostStatus.ACTIVE;
        this.isAccepted = isAccepted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void update(Long subjectId, String title, String content, int viewCount,
                       PostStatus status, boolean isAccepted, LocalDateTime updatedAt) {
        this.subjectId = subjectId;
        this.title = title;
        this.content = content;
        this.viewCount = viewCount;
        this.status = status == null ? this.status : status;
        this.isAccepted = isAccepted;
        this.updatedAt = updatedAt;
    }
    public boolean isAdminDeleted() {
        return this.status == PostStatus.ADMIN_DELETED;
    }

    public boolean isActive() {
        return this.status == PostStatus.ACTIVE;
    }

    public void softDeleteByAdmin(LocalDateTime updatedAt) {
        this.status = PostStatus.ADMIN_DELETED;
        this.updatedAt = updatedAt;
    }
}
