package com.wanted.backend.domain.community.presentation.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateStudyRequest(
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @NotBlank(message = "과목은 필수입니다.")
        String subject,

        @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
        int maxCount,

        @NotBlank(message = "내용은 필수입니다.")
        String content
) {}