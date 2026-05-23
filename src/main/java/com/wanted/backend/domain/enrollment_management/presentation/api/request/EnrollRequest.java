package com.wanted.backend.domain.enrollment_management.presentation.api.request;

import com.wanted.backend.domain.enrollment_management.application.command.EnrollCommand;
import jakarta.validation.constraints.NotNull;

public record EnrollRequest(
        @NotNull(message = "강의 ID는 필수입니다.")
        Long courseId
) {
    public EnrollCommand toCommand(Long userId) {
        return new EnrollCommand(userId, courseId);
    }
}
