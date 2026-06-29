package com.wanted.backend.domain.study_timer.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

@Schema(description = "순공시간 세션 종료 요청")
public record EndStudyTimerSessionRequest(
        @Schema(description = "종료 시각 (클라이언트 기준 ISO-8601 오프셋 포함)", example = "2026-06-28T11:00:00+09:00")
        @NotNull
        OffsetDateTime endedAt
) {
}
