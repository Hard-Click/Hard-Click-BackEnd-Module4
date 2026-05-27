package com.wanted.backend.domain.community.domain.model;

import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import java.time.LocalDateTime;


public class Comment {

    private Long id;
    private Long postId;
    private Long authorId;
    private Long parentId;
    private String content;
    private boolean isAccepted;
    private boolean isDeleted;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Comment(Long id, Long postId, Long authorId, Long parentId,
                    String content, boolean isAccepted, boolean isDeleted,
                    String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.postId = postId;
        this.authorId = authorId;
        this.parentId = parentId;
        this.content = content;
        this.isAccepted = isAccepted;
        this.isDeleted = isDeleted;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 새 댓글 생성
    public static Comment create(Long postId, Long authorId, Long parentId,
                                 String content, String imageUrl) {
        return new Comment(null, postId, authorId, parentId, content,
                false, false, imageUrl, LocalDateTime.now(), LocalDateTime.now());
    }

    // DB에서 꺼낸 데이터를 도메인 객체로 복원
    public static Comment restore(Long id, Long postId, Long authorId, Long parentId,
                                  String content, boolean isAccepted, boolean isDeleted,
                                  String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Comment(id, postId, authorId, parentId, content,
                isAccepted, isDeleted, imageUrl, createdAt, updatedAt);
    }

    //채택
    public void accept() {
        this.isAccepted = true;
        this.updatedAt = LocalDateTime.now();
    }

    //수정 가능 여부 검증
    public void validateUpdatable(Long memberId) {
        if (!this.authorId.equals(memberId)) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_AUTHORIZED);
        }
        if (this.isAccepted) {
            throw new BusinessException(ErrorCode.COMMENT_ACCEPTED_CANNOT_MODIFY);
        }
    }

    //삭제 가능 여부 검증
    public void validateDeletable(Long memberId) {
        if (!this.authorId.equals(memberId)) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_AUTHORIZED);
        }
        if (this.isAccepted) {
            throw new BusinessException(ErrorCode.COMMENT_ACCEPTED_CANNOT_DELETE);
        }
    }

    //댓글 수정
    public void update(String content, String imageUrl) {
        this.content = content;
        this.imageUrl = imageUrl;
        this.updatedAt = LocalDateTime.now();
    }

    //댓글 삭제
    public void softDelete() {
        this.content = "삭제된 댓글입니다.";
        this.isDeleted = true;
        this.imageUrl = null;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public Long getAuthorId() { return authorId; }
    public Long getParentId() { return parentId; }
    public String getContent() { return content; }
    public boolean isAccepted() { return isAccepted; }
    public boolean isDeleted() { return isDeleted; }
    public String getImageUrl() { return imageUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}