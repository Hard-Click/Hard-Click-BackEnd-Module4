package com.wanted.backend.domain.enrollment_management.presentation.api.response;

import com.wanted.backend.domain.enrollment_management.application.dto.MyEnrollmentResult;
import com.wanted.backend.domain.enrollment_management.domain.model.EnrollmentStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record MyEnrollmentResponse(
        Long enrollmentId,
        Long courseId,
        String courseTitle,
        EnrollmentStatus status,
        Instant enrolledAt,
        LocalDateTime expiredAt,
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
