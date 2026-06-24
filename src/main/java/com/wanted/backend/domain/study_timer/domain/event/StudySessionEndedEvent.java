package com.wanted.backend.domain.study_timer.domain.event;

import com.wanted.backend.global.domain.DomainEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record StudySessionEndedEvent(
        Long memberId,
        LocalDate studyDate,
        Integer deltaStudySeconds,
        OffsetDateTime endedAt,
        Instant occurredAt
) implements DomainEvent {

    public static StudySessionEndedEvent of(
            Long memberId,
            LocalDate studyDate,
            Integer deltaStudySeconds,
            OffsetDateTime endedAt
    ) {
        return new StudySessionEndedEvent(
                memberId,
                studyDate,
                deltaStudySeconds,
                endedAt,
                Instant.now()
        );
    }
}
