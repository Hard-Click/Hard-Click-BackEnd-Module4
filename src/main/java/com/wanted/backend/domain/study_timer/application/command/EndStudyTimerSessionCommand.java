package com.wanted.backend.domain.study_timer.application.command;

import java.time.OffsetDateTime;

public record EndStudyTimerSessionCommand(
        Long memberId,
        Long sessionId,
        OffsetDateTime endedAt
) {
}
