package com.wanted.backend.domain.study_timer.presentation.api.response;

import com.wanted.backend.domain.study_timer.application.usecase.ResumeStudyTimerSessionUseCase.StudyTimerSessionResumeView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "순공시간 세션 재개 응답")
public record StudyTimerSessionResumeResponse(
        @Schema(description = "세션 ID", example = "55")
        Long sessionId,

        @Schema(description = "세션 상태", example = "RUNNING")
        String status,

        @Schema(description = "누적 순공시간 (초)", example = "1800")
        Integer accumulatedStudySeconds,

        @Schema(description = "재개 시각", example = "2026-06-28T10:40:00+09:00")
        OffsetDateTime resumedAt
) {
    public static StudyTimerSessionResumeResponse from(StudyTimerSessionResumeView view) {
        return new StudyTimerSessionResumeResponse(
                view.sessionId(),
                view.status(),
                view.accumulatedStudySeconds(),
                view.resumedAt()
        );
    }
}
