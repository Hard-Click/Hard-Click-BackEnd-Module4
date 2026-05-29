package com.wanted.backend.domain.cource.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "섹션 수정 요청")
public record UpdateSectionRequest(
        @Schema(description = "섹션 ID (null이면 신규 섹션)", example = "1")
        Long id,

        @Schema(description = "섹션 제목", example = "섹션 1: 함수의 극한")
        @NotBlank(message = "섹션 제목은 필수입니다.")
        String title,

        @Schema(description = "섹션 순서 (0-based)", example = "0")
        int orderIndex,

        @Schema(description = "레슨 목록")
        @Valid
        List<UpdateLessonRequest> lessons
) {}
