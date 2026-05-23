package com.wanted.backend.domain.enrollment_management.domain.model;

import java.time.Instant;

public class Enrollment {

    private Long id;
    private Long userId;
    private Long courseId;
    private Instant enrolledAt;

    private Enrollment() {}

    public static Enrollment create(Long userId, Long courseId, Instant now) {
        Enrollment enrollment = new Enrollment();
        enrollment.userId = userId;
        enrollment.courseId = courseId;
        enrollment.enrolledAt = now;
        return enrollment;
    }

    public static Enrollment restore(Long id, Long userId, Long courseId, Instant enrolledAt) {
        Enrollment enrollment = new Enrollment();
        enrollment.id = id;
        enrollment.userId = userId;
        enrollment.courseId = courseId;
        enrollment.enrolledAt = enrolledAt;
        return enrollment;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getCourseId() { return courseId; }
    public Instant getEnrolledAt() { return enrolledAt; }
}
