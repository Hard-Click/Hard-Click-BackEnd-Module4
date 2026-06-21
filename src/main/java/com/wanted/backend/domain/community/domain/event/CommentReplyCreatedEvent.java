package com.wanted.backend.domain.community.domain.event;

import com.wanted.backend.global.domain.DomainEvent;

import java.time.Instant;

// 댓글에 대댓글이 달렸을 때 발행 → 부모 댓글 작성자에게 알림
public record CommentReplyCreatedEvent(
        Long parentCommentAuthorId,
        Long replyAuthorId,
        Long postId,
        Long replyCommentId,
        Instant occurredAt
) implements DomainEvent {

    public static CommentReplyCreatedEvent of(Long parentCommentAuthorId, Long replyAuthorId,
                                              Long postId, Long replyCommentId) {
        return new CommentReplyCreatedEvent(parentCommentAuthorId, replyAuthorId,
                postId, replyCommentId, Instant.now());
    }
}