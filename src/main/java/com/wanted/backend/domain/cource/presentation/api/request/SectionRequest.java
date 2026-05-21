package com.wanted.backend.domain.cource.presentation.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.ArrayList;
import java.util.List;

public record SectionRequest(
        @NotBlank(message = "섹션 제목은 필수입니다.")
        String title,

        @PositiveOrZero
        int orderIndex,

        @Valid
        List<LessonRequest> lessons
) {
    public List<LessonRequest> lessons() {
        return lessons == null ? new ArrayList<>() : lessons;
    }
}
