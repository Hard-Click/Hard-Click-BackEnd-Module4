package com.wanted.backend.domain.enrollment_management.presentation.api.request;

import com.wanted.backend.domain.enrollment_management.application.command.EnrollCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "수강신청 요청")
public record EnrollRequest(
        @Schema(description = "수강신청할 강의 ID", example = "5")
        @NotNull(message = "강의 ID는 필수입니다.")
        Long courseId
) {
    public EnrollCommand toCommand(Long memberId) {
        return new EnrollCommand(memberId, courseId);
    }
}
