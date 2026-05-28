package com.wanted.backend.domain.learning_activity.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "마지막 재생 위치 저장 요청")
public record SaveVideoPositionRequest(
        @Schema(description = "마지막 재생 위치(초)", example = "142")
        @NotNull
        @PositiveOrZero
        Integer positionSeconds
) {
}
