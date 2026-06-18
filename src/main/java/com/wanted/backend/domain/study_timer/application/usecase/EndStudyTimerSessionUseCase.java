package com.wanted.backend.domain.study_timer.application.usecase;

import com.wanted.backend.domain.study_timer.application.command.EndStudyTimerSessionCommand;

public interface EndStudyTimerSessionUseCase {

    StudyTimerSessionEndView handle(EndStudyTimerSessionCommand command);

    record StudyTimerSessionEndView(
            Long sessionId,
            Integer studySeconds,
            String status
    ) {
    }
}
