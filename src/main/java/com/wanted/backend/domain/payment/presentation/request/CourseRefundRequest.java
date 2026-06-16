package com.wanted.backend.domain.payment.presentation.request;

import jakarta.validation.constraints.NotNull;

public record CourseRefundRequest(
        @NotNull(message = "강의 ID는 필수입니다.")
        Long courseId,
        String reason
) {}
