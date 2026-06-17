package com.wanted.backend.domain.study_timer.application.usecase;

import com.wanted.backend.domain.study_timer.application.command.StartStudyTimerSessionCommand;

import java.time.OffsetDateTime;

public interface StartStudyTimerSessionUseCase {

    StudyTimerSessionStartView handle(StartStudyTimerSessionCommand command);

    record StudyTimerSessionStartView(
            Long sessionId,
            String status,
            OffsetDateTime startedAt
    ) {
    }
}
