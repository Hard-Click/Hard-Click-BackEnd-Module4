package com.wanted.backend.domain.enrollment_management.domain.repository;

import com.wanted.backend.domain.enrollment_management.domain.model.Enrollment;
import com.wanted.backend.domain.enrollment_management.domain.model.EnrollmentStatus;

import java.util.List;

public interface EnrollmentRepository {
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    Enrollment save(Enrollment enrollment);
    List<Enrollment> findByUserId(Long userId);
    List<Enrollment> findByUserIdAndStatus(Long userId, EnrollmentStatus status);
}
