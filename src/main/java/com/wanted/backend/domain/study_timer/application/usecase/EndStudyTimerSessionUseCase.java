package com.wanted.backend.domain.study_timer.application.usecase;

import com.wanted.backend.domain.study_timer.application.command.EndStudyTimerSessionCommand;

import java.time.OffsetDateTime;

public interface EndStudyTimerSessionUseCase {

    StudyTimerSessionEndView handle(EndStudyTimerSessionCommand command);

    record StudyTimerSessionEndView(
            Long sessionId,
            Integer accumulatedStudySeconds,
            String status,
            OffsetDateTime endedAt
    ) {
    }
}
