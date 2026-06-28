package com.wanted.backend.domain.study_timer.presentation.api.response;

import com.wanted.backend.domain.study_timer.application.usecase.StartStudyTimerSessionUseCase.StudyTimerSessionStartView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "순공시간 세션 시작 응답")
public record StudyTimerSessionStartResponse(
        @Schema(description = "세션 ID", example = "55")
        Long sessionId,

        @Schema(description = "세션 상태", example = "RUNNING")
        String status,

        @Schema(description = "시작 시각", example = "2026-06-28T10:00:00+09:00")
        OffsetDateTime startedAt
) {
    public static StudyTimerSessionStartResponse from(StudyTimerSessionStartView view) {
        return new StudyTimerSessionStartResponse(
                view.sessionId(),
                view.status(),
                view.startedAt()
        );
    }
}
