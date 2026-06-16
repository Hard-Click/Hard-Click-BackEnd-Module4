package com.wanted.backend.domain.identity.domain.event;

import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import com.wanted.backend.domain.identity.domain.model.MemberStatusChangeReason;
import com.wanted.backend.global.domain.DomainEvent;

import java.time.Instant;
import java.time.LocalDateTime;

public class MemberStatusChangedEvent implements DomainEvent {

    private final Long memberId;
    private final MemberStatus previousStatus;
    private final MemberStatus status;
    private final MemberStatusChangeReason reason;
    private final String message;
    private final LocalDateTime changedAt;
    private final Instant occurredAt;

    public MemberStatusChangedEvent(
            Long memberId,
            MemberStatus previousStatus,
            MemberStatus status,
            MemberStatusChangeReason reason,
            String message,
            LocalDateTime changedAt
    ) {
        this.memberId = memberId;
        this.previousStatus = previousStatus;
        this.status = status;
        this.reason = reason;
        this.message = message;
        this.changedAt = changedAt;
        this.occurredAt = Instant.now();
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }

    public Long getMemberId() {
        return memberId;
    }

    public MemberStatus getPreviousStatus() {
        return previousStatus;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public MemberStatusChangeReason getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
