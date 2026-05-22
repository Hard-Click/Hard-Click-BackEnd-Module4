package com.wanted.backend.domain.enrollment_management.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataEnrollmentRepository extends JpaRepository<EnrollmentJpaEntity, Long> {
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
