package com.wanted.backend.domain.study_timer.presentation.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.OffsetDateTime;

public record PauseStudyTimerSessionRequest(
        @NotNull
        @PastOrPresent
        OffsetDateTime pausedAt
) {
}
