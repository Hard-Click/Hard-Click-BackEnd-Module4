package com.wanted.backend.domain.enrollment_management.presentation.api.response;

import com.wanted.backend.domain.enrollment_management.application.dto.MyEnrollmentResult;
import com.wanted.backend.domain.enrollment_management.domain.model.EnrollmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "내 수강 목록 항목 응답")
public record MyEnrollmentResponse(
        @Schema(description = "수강 등록 ID", example = "101")
        Long enrollmentId,

        @Schema(description = "강의 ID", example = "5")
        Long courseId,

        @Schema(description = "강의명", example = "2027 수능 수학Ⅱ 미적분 실전 킬러 특강")
        String courseTitle,

        @Schema(description = "수강 상태 (IN_PROGRESS / COMPLETED)", example = "IN_PROGRESS")
        EnrollmentStatus status,

        @Schema(description = "수강신청 일시")
        Instant enrolledAt,

        @Schema(description = "수강 만료 일시")
        LocalDateTime expiredAt,

        @Schema(description = "진도율 (%)", example = "42")
        int progressPercent
) {
    public static MyEnrollmentResponse from(MyEnrollmentResult result) {
        return new MyEnrollmentResponse(
                result.enrollmentId(),
                result.courseId(),
                result.courseTitle(),
                result.status(),
                result.enrolledAt(),
                result.expiredAt(),
                result.progressPercent()
        );
    }

    public static List<MyEnrollmentResponse> from(List<MyEnrollmentResult> results) {
        return results.stream().map(MyEnrollmentResponse::from).toList();
    }
}
