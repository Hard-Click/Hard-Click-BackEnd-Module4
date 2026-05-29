package com.wanted.backend.domain.cource.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "레슨(회차) 요청")
public record LessonRequest(
        @Schema(description = "레슨 제목", example = "OT 및 학습 방향")
        @NotBlank(message = "회차 제목은 필수입니다.")
        String title,

        @Schema(description = "레슨 설명", example = "강의 전체 구성과 학습 방향을 안내합니다.")
        String description,

        @Schema(description = "레슨 순서 (0-based)", example = "0")
        @PositiveOrZero
        int orderIndex,

        @Schema(description = "영상 재생시간 (초, 프론트에서 메타데이터 추출)", example = "323")
        Integer durationSeconds
) {}
