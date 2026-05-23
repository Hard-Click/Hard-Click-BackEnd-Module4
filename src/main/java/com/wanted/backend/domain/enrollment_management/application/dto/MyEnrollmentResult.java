package com.wanted.backend.domain.enrollment_management.application.dto;

import com.wanted.backend.domain.enrollment_management.domain.model.EnrollmentStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record MyEnrollmentResult(
        Long enrollmentId,
        Long courseId,
        String courseTitle,
        EnrollmentStatus status,
        Instant enrolledAt,
        LocalDateTime expiredAt,
        int progressPercent
) {}
