package com.wanted.backend.domain.enrollment_management.domain.repository;

import com.wanted.backend.domain.enrollment_management.domain.model.Enrollment;

public interface EnrollmentRepository {
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    Enrollment save(Enrollment enrollment);
}
