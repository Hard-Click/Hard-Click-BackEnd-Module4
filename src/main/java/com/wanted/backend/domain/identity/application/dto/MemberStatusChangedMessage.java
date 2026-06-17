package com.wanted.backend.domain.identity.application.dto;

import com.wanted.backend.domain.identity.domain.event.MemberStatusChangedEvent;
import com.wanted.backend.domain.identity.domain.model.MemberStatus;
import com.wanted.backend.domain.identity.domain.model.MemberStatusChangeReason;

import java.time.LocalDateTime;

public record MemberStatusChangedMessage(
        Long memberId,
        MemberStatus previousStatus,
        MemberStatus status,
        MemberStatusChangeReason reason,
        String message,
        LocalDateTime occurredAt
) {
    public static MemberStatusChangedMessage from(MemberStatusChangedEvent event) {
        return new MemberStatusChangedMessage(
                event.getMemberId(),
                event.getPreviousStatus(),
                event.getStatus(),
                event.getReason(),
                event.getMessage(),
                event.getChangedAt()
        );
    }
}
