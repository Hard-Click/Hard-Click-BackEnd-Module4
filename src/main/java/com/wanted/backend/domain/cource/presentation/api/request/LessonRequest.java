package com.wanted.backend.domain.cource.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record LessonRequest(
        @NotBlank(message = "회차 제목은 필수입니다.")
        String title,

        String description,

        @PositiveOrZero
        int orderIndex,

        Integer durationSeconds   // 프론트에서 메타데이터로 추출한 영상 길이 (초)
) {}
