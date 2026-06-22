package com.wanted.backend.domain.study_timer.application.command;

import java.time.OffsetDateTime;

public record SaveStudyTimerHeartbeatCommand(
        Long memberId,
        Long sessionId,
        OffsetDateTime heartbeatAt
) {
}
