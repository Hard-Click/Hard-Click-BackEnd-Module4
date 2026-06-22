package com.wanted.backend.domain.community.domain.event;

import com.wanted.backend.global.domain.DomainEvent;

import java.time.Instant;

// 신고가 접수됐을 때 발행 → 관리자에게 알림
public record ReportCreatedEvent(
        Long reportId,
        String targetType,
        Long targetId,
        Instant occurredAt
) implements DomainEvent {

    public static ReportCreatedEvent of(Long reportId, String targetType, Long targetId) {
        return new ReportCreatedEvent(reportId, targetType, targetId, Instant.now());
    }
}