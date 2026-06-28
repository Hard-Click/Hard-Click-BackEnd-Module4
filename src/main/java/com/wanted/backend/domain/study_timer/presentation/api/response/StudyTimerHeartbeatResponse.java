package com.wanted.backend.domain.study_timer.presentation.api.response;

import com.wanted.backend.domain.study_timer.application.usecase.SaveStudyTimerHeartbeatUseCase.StudyTimerHeartbeatView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "순공시간 하트비트 저장 응답")
public record StudyTimerHeartbeatResponse(
        @Schema(description = "세션 ID", example = "55")
        Long sessionId,

        @Schema(description = "세션 상태", example = "RUNNING")
        String status,

        @Schema(description = "누적 순공시간 (초)", example = "1800")
        Integer accumulatedStudySeconds,

        @Schema(description = "하트비트 시각", example = "2026-06-28T10:20:00+09:00")
        OffsetDateTime heartbeatAt
) {
    public static StudyTimerHeartbeatResponse from(StudyTimerHeartbeatView view) {
        return new StudyTimerHeartbeatResponse(
                view.sessionId(),
                view.status(),
                view.accumulatedStudySeconds(),
                view.heartbeatAt()
        );
    }
}
