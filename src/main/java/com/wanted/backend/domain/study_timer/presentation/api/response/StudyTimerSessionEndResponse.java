package com.wanted.backend.domain.study_timer.presentation.api.response;

import com.wanted.backend.domain.study_timer.application.usecase.EndStudyTimerSessionUseCase.StudyTimerSessionEndView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "순공시간 세션 종료 응답")
public record StudyTimerSessionEndResponse(
        @Schema(description = "세션 ID", example = "55")
        Long sessionId,

        @Schema(description = "최종 누적 순공시간 (초)", example = "3600")
        Integer accumulatedStudySeconds,

        @Schema(description = "세션 상태", example = "ENDED")
        String status,

        @Schema(description = "종료 시각", example = "2026-06-28T11:00:00+09:00")
        OffsetDateTime endedAt
) {
    public static StudyTimerSessionEndResponse from(StudyTimerSessionEndView view) {
        return new StudyTimerSessionEndResponse(
                view.sessionId(),
                view.accumulatedStudySeconds(),
                view.status(),
                view.endedAt()
        );
    }
}
