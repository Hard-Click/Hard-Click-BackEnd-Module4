package com.wanted.backend.domain.payment.presentation.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateCourseOrderRequest(
        @NotEmpty(message = "강의 ID 목록은 비어 있을 수 없습니다.")
        List<Long> courseIds
) {}
