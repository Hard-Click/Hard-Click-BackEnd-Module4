package com.wanted.backend.domain.study_timer.application.usecase;

import com.wanted.backend.domain.study_timer.application.command.ResumeStudyTimerSessionCommand;

import java.time.OffsetDateTime;

public interface ResumeStudyTimerSessionUseCase {

    StudyTimerSessionResumeView handle(ResumeStudyTimerSessionCommand command);

    record StudyTimerSessionResumeView(
            Long sessionId,
            String status,
            Integer accumulatedStudySeconds,
            OffsetDateTime resumedAt
    ) {
    }
}
