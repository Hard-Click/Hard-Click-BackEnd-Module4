package com.wanted.backend.domain.payment.application.port;

import com.wanted.backend.domain.enrollment_management.domain.model.EnrollmentStatus;

import java.time.Instant;
import java.util.Optional;

public interface EnrollmentRefundPort {

    Optional<EnrollmentData> findByMemberIdAndCourseId(Long memberId, Long courseId);

    void updateStatus(Long enrollmentId, EnrollmentStatus status);

    record EnrollmentData(
            Long enrollmentId,
            Long memberId,
            Long courseId,
            Instant enrolledAt,
            EnrollmentStatus status
    ) {}
}
