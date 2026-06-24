package com.wanted.backend.domain.study_timer.application.command;

import java.time.OffsetDateTime;

public record PauseStudyTimerSessionCommand(
        Long memberId,
        Long sessionId,
        OffsetDateTime pausedAt
) {
}
