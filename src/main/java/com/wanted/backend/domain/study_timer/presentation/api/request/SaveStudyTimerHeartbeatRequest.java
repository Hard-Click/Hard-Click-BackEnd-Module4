package com.wanted.backend.domain.study_timer.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.OffsetDateTime;

@Schema(description = "순공시간 하트비트 저장 요청")
public record SaveStudyTimerHeartbeatRequest(
        @Schema(description = "하트비트 시각 (클라이언트 기준 ISO-8601 오프셋 포함, 현재 이전 또는 현재)", example = "2026-06-28T10:20:00+09:00")
        @NotNull
        @PastOrPresent
        OffsetDateTime heartbeatAt
) {
}
