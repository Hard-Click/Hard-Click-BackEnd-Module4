package com.wanted.backend.domain.cource.domain.event;

import com.wanted.backend.global.domain.DomainEvent;

import java.time.Instant;

// 강사가 강좌를 개설했을 때 발행 → 관리자에게 알림
public record CourseCreatedEvent(
        Long courseId,
        Long instructorId,
        String title,
        Instant occurredAt
) implements DomainEvent {

    public static CourseCreatedEvent of(Long courseId, Long instructorId, String title) {
        return new CourseCreatedEvent(courseId, instructorId, title, Instant.now());
    }
}