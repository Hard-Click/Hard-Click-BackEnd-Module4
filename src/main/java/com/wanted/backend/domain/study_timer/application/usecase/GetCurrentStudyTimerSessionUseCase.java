package com.wanted.backend.domain.study_timer.application.usecase;

import com.wanted.backend.domain.study_timer.application.query.GetCurrentStudyTimerSessionQuery;

import java.time.OffsetDateTime;

public interface GetCurrentStudyTimerSessionUseCase {

    CurrentStudyTimerSessionView handle(GetCurrentStudyTimerSessionQuery query);

    record CurrentStudyTimerSessionView(
            Long sessionId,
            String status,
            OffsetDateTime startedAt,
            Integer accumulatedStudySeconds
    ) {
    }
}
