package com.wanted.backend.domain.study_timer.presentation.api.response;

import com.wanted.backend.domain.study_timer.application.usecase.GetCurrentStudyTimerSessionUseCase.CurrentStudyTimerSessionView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "실행 중 순공시간 세션 응답 (실행 중인 세션이 없으면 data는 null)")
public record CurrentStudyTimerSessionResponse(
        @Schema(description = "세션 ID", example = "55")
        Long sessionId,

        @Schema(description = "세션 상태", example = "RUNNING")
        String status,

        @Schema(description = "시작 시각", example = "2026-06-28T10:00:00+09:00")
        OffsetDateTime startedAt,

        @Schema(description = "누적 순공시간 (초)", example = "1800")
        Integer accumulatedStudySeconds
) {
    public static CurrentStudyTimerSessionResponse from(CurrentStudyTimerSessionView view) {
        if (view == null) {
            return null;
        }

        return new CurrentStudyTimerSessionResponse(
                view.sessionId(),
                view.status(),
                view.startedAt(),
                view.accumulatedStudySeconds()
        );
    }
}
