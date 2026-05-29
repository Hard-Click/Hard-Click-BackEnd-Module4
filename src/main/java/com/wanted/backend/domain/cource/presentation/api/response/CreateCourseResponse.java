package com.wanted.backend.domain.cource.presentation.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강의 등록 응답")
public record CreateCourseResponse(
        @Schema(description = "생성된 강의 ID", example = "10")
        Long courseId
) {}
