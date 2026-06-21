package com.wanted.backend.domain.community.domain.event;

import com.wanted.backend.global.domain.DomainEvent;

import java.time.Instant;

// 댓글이 채택됐을 때 발행 → 채택된 댓글 작성자에게 알림
public record CommentAcceptedEvent(
        Long commentAuthorId,
        Long postId,
        Long commentId,
        Instant occurredAt
) implements DomainEvent {

    public static CommentAcceptedEvent of(Long commentAuthorId, Long postId, Long commentId) {
        return new CommentAcceptedEvent(commentAuthorId, postId, commentId, Instant.now());
    }
}