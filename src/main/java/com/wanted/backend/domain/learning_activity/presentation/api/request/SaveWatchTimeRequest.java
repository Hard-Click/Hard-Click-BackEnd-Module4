package com.wanted.backend.domain.learning_activity.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "영상 시청 시간 누적 저장 요청")
public record SaveWatchTimeRequest(
        @Schema(description = "이번 요청에서 추가로 누적할 시청 시간(초)", example = "30")
        @NotNull
        @Positive
        Integer watchTimeSeconds
) {
}
