package com.wanted.backend.domain.study_timer.presentation.api.request;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record StartStudyTimerSessionRequest(
        @NotNull
        OffsetDateTime startedAt
) {
}
