package com.wanted.backend.domain.learning_activity.domain.event;

import com.wanted.backend.global.domain.DomainEvent;

import java.time.Instant;

public record VideoCompletedEvent(
        Long memberId,
        Long videoId,
        Long courseId,
        Instant occurredAt
) implements DomainEvent {

    public static VideoCompletedEvent of(Long memberId, Long videoId, Long courseId) {
        return new VideoCompletedEvent(memberId, videoId, courseId, Instant.now());
    }
}
