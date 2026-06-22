package com.wanted.backend.domain.community.domain.event;

import com.wanted.backend.global.domain.DomainEvent;

import java.time.Instant;

// 게시글에 댓글이 달렸을 때 발행 → 게시글 작성자에게 알림
public record PostCommentCreatedEvent(
        Long postAuthorId,
        Long commentAuthorId,
        Long postId,
        Long commentId,
        Instant occurredAt
) implements DomainEvent {

    public static PostCommentCreatedEvent of(Long postAuthorId, Long commentAuthorId,
                                             Long postId, Long commentId) {
        return new PostCommentCreatedEvent(postAuthorId, commentAuthorId,
                postId, commentId, Instant.now());
    }
}