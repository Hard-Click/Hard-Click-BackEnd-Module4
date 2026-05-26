package com.wanted.backend.domain.community.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;


@Entity
@Table(name = "comments")
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
                            String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.postId = postId;
        this.authorId = authorId;
        this.parentId = parentId;
        this.content = content;
        this.isAccepted = isAccepted;
        this.isDeleted = isDeleted;
        this.acceptCount = 0;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void accept(LocalDateTime updatedAt) {
        this.isAccepted = true;
        this.updatedAt = updatedAt;
    }
}