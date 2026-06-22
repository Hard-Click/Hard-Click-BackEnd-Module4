package com.wanted.backend.domain.study_timer.application.usecase;

import com.wanted.backend.domain.study_timer.application.command.SaveStudyTimerHeartbeatCommand;

import java.time.OffsetDateTime;

public interface SaveStudyTimerHeartbeatUseCase {

    StudyTimerHeartbeatView handle(SaveStudyTimerHeartbeatCommand command);

    record StudyTimerHeartbeatView(
            Long sessionId,
            String status,
            Integer accumulatedStudySeconds,
            OffsetDateTime heartbeatAt
    ) {
    }
}
