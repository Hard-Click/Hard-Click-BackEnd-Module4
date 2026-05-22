package com.wanted.backend.domain.identity.domain.event;

import com.wanted.backend.global.domain.DomainEvent;
import java.time.Instant;
import java.time.LocalDateTime;

public class MemberLoggedInEvent implements DomainEvent {
    private final Long memberId;
    private final LocalDateTime loginTime;
    private final Instant occurredAt;

    public MemberLoggedInEvent(Long memberId, LocalDateTime loginTime) {
        this.memberId = memberId;
        this.loginTime = loginTime;
        this.occurredAt = Instant.now();
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }

    public Long getMemberId() {
        return memberId;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }
}