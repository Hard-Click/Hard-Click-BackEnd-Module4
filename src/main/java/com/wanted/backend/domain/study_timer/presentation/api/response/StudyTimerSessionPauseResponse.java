package com.wanted.backend.domain.study_timer.presentation.api.response;

import com.wanted.backend.domain.study_timer.application.usecase.PauseStudyTimerSessionUseCase.StudyTimerSessionPauseView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "순공시간 세션 일시정지 응답")
public record StudyTimerSessionPauseResponse(
        @Schema(description = "세션 ID", example = "55")
        Long sessionId,

        @Schema(description = "세션 상태", example = "PAUSED")
        String status,

        @Schema(description = "누적 순공시간 (초)", example = "1800")
        Integer accumulatedStudySeconds,

        @Schema(description = "일시정지 시각", example = "2026-06-28T10:30:00+09:00")
        OffsetDateTime pausedAt
) {
    public static StudyTimerSessionPauseResponse from(StudyTimerSessionPauseView view) {
        return new StudyTimerSessionPauseResponse(
                view.sessionId(),
                view.status(),
                view.accumulatedStudySeconds(),
                view.pausedAt()
        );
    }
}
