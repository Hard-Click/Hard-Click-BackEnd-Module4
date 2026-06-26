package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.Comment;
import com.wanted.backend.domain.community.domain.model.CommentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comments_post_id", columnList = "post_id"),
        @Index(name = "idx_comments_author_id", columnList = "author_id")
})
@Getter
public class CommentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_accepted", nullable = false)
    private boolean isAccepted;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CommentStatus status = CommentStatus.ACTIVE;

    @Column(name = "accept_count", nullable = false)
    private int acceptCount;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CommentJpaEntity() {}

    public CommentJpaEntity(Long postId, Long authorId, Long parentId,
                            String content, boolean isAccepted, boolean isDeleted,
                            CommentStatus status, String imageUrl,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        CommentStatus resolvedStatus = status == null ? legacyStatus(isDeleted) : status;

        this.postId = postId;
        this.authorId = authorId;
        this.parentId = parentId;
        this.content = content;
        this.isAccepted = isAccepted;
        this.isDeleted = resolvedStatus != CommentStatus.ACTIVE;
        this.status = resolvedStatus;
        this.acceptCount = 0;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public CommentJpaEntity(Long postId, Long authorId, Long parentId,
                            String content, boolean isAccepted, boolean isDeleted,
                            String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(postId, authorId, parentId, content, isAccepted, isDeleted,
                isDeleted ? CommentStatus.DELETED : CommentStatus.ACTIVE,
                imageUrl, createdAt, updatedAt);
    }

    public void accept(LocalDateTime updatedAt) {
        this.isAccepted = true;
        this.updatedAt = updatedAt;
    }

    public void update(String content, String imageUrl, LocalDateTime updatedAt) {
        this.content = content;
        this.imageUrl = imageUrl;
        this.updatedAt = updatedAt;
    }

    public void softDelete(LocalDateTime updatedAt) {
        this.content = "삭제된 댓글입니다.";
        this.isDeleted = true;
        this.status = CommentStatus.DELETED;
        this.imageUrl = null;
        this.updatedAt = updatedAt;
    }

    public void softDeleteByAdmin(LocalDateTime updatedAt) {
        this.isDeleted = true;
        this.status = CommentStatus.ADMIN_DELETED;
        this.imageUrl = null;
        this.updatedAt = updatedAt;
    }

    private static CommentStatus legacyStatus(boolean isDeleted) {
        return isDeleted ? CommentStatus.DELETED : CommentStatus.ACTIVE;
    }
}
