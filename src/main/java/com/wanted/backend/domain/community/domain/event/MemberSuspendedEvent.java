package com.wanted.backend.domain.community.domain.event;

import com.wanted.backend.global.domain.DomainEvent;

import java.time.Instant;

public record MemberSuspendedEvent(
        Long memberId,
        Instant occurredAt
) implements DomainEvent {

    public static MemberSuspendedEvent of(Long memberId) {
        return new MemberSuspendedEvent(memberId, Instant.now());
    }
}