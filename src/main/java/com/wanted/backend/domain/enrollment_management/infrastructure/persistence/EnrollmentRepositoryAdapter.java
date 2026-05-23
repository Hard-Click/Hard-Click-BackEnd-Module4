package com.wanted.backend.domain.enrollment_management.infrastructure.persistence;

import com.wanted.backend.domain.enrollment_management.domain.model.Enrollment;
import com.wanted.backend.domain.enrollment_management.domain.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrollmentRepositoryAdapter implements EnrollmentRepository {

    private final SpringDataEnrollmentRepository jpaRepository;

    @Override
    public boolean existsByUserIdAndCourseId(Long userId, Long courseId) {
        return jpaRepository.existsByMemberIdAndCourseId(userId, courseId);
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        return jpaRepository.save(EnrollmentJpaEntity.from(enrollment)).toDomain();
    }
}
