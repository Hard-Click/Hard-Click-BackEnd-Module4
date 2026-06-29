package com.wanted.backend.domain.study_timer.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.OffsetDateTime;

@Schema(description = "순공시간 세션 재개 요청")
public record ResumeStudyTimerSessionRequest(
        @Schema(description = "재개 시각 (클라이언트 기준 ISO-8601 오프셋 포함, 현재 이전 또는 현재)", example = "2026-06-28T10:40:00+09:00")
        @NotNull
        @PastOrPresent
        OffsetDateTime resumedAt
) {
}
