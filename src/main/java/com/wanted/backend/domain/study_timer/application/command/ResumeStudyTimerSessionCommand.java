package com.wanted.backend.domain.study_timer.application.command;

import java.time.OffsetDateTime;

public record ResumeStudyTimerSessionCommand(
        Long memberId,
        Long sessionId,
        OffsetDateTime resumedAt
) {
}
