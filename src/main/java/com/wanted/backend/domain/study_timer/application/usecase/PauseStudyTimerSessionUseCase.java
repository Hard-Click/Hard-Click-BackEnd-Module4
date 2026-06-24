package com.wanted.backend.domain.study_timer.application.usecase;

import com.wanted.backend.domain.study_timer.application.command.PauseStudyTimerSessionCommand;

import java.time.OffsetDateTime;

public interface PauseStudyTimerSessionUseCase {

    StudyTimerSessionPauseView handle(PauseStudyTimerSessionCommand command);

    record StudyTimerSessionPauseView(
            Long sessionId,
            String status,
            Integer accumulatedStudySeconds,
            OffsetDateTime pausedAt
    ) {
    }
}
