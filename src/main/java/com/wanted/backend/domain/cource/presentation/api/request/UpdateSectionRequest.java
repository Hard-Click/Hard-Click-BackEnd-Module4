package com.wanted.backend.domain.cource.presentation.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UpdateSectionRequest(
        Long id,             // null = 신규 섹션

        @NotBlank(message = "섹션 제목은 필수입니다.")
        String title,

        int orderIndex,

        @Valid
        List<UpdateLessonRequest> lessons
) {}
