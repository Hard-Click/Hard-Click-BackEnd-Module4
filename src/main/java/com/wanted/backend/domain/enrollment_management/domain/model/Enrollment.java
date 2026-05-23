package com.wanted.backend.domain.enrollment_management.domain.model;

import java.time.Instant;
import java.time.LocalDateTime;

public class Enrollment {

    private Long id;
    private Long userId;
    private Long courseId;
    private Instant enrolledAt;
    private EnrollmentStatus status;
    private LocalDateTime expiredAt;

    private Enrollment() {}

    public static Enrollment create(Long userId, Long courseId, Instant now) {
        Enrollment enrollment = new Enrollment();
        enrollment.userId = userId;
        enrollment.courseId = courseId;
        enrollment.enrolledAt = now;
        enrollment.status = EnrollmentStatus.IN_PROGRESS;
        return enrollment;
    }

    public static Enrollment restore(Long id, Long userId, Long courseId, Instant enrolledAt,
                                     EnrollmentStatus status, LocalDateTime expiredAt) {
        Enrollment enrollment = new Enrollment();
        enrollment.id = id;
        enrollment.userId = userId;
        enrollment.courseId = courseId;
        enrollment.enrolledAt = enrolledAt;
        enrollment.status = status;
        enrollment.expiredAt = expiredAt;
        return enrollment;
    }

    /**
     * 만료일 기준 상태 자동 계산:
     * - expiredAt이 현재 시각 이전이면 EXPIRED
     * - 그 외 저장된 status 반환
     */
    public EnrollmentStatus getEffectiveStatus() {
        if (expiredAt != null && expiredAt.isBefore(LocalDateTime.now())) {
            return EnrollmentStatus.EXPIRED;
        }
        return status;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getCourseId() { return courseId; }
    public Instant getEnrolledAt() { return enrolledAt; }
    public EnrollmentStatus getStatus() { return status; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
}
