package com.wanted.backend.domain.study_timer.application.command;

import java.time.OffsetDateTime;

public record StartStudyTimerSessionCommand(
        Long memberId,
        OffsetDateTime startedAt
) {
}
