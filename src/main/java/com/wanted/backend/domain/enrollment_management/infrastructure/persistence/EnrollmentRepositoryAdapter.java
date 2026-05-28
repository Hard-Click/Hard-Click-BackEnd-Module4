package com.wanted.backend.domain.enrollment_management.infrastructure.persistence;

import com.wanted.backend.domain.enrollment_management.domain.model.Enrollment;
import com.wanted.backend.domain.enrollment_management.domain.model.EnrollmentStatus;
import com.wanted.backend.domain.enrollment_management.domain.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

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

    @Override
    public List<Enrollment> findByUserId(Long userId) {
        return jpaRepository.findByMemberId(userId).stream()
                .map(EnrollmentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Enrollment> findByUserIdAndStatus(Long userId, EnrollmentStatus status) {
        return jpaRepository.findByMemberIdAndStatus(userId, status).stream()
                .map(EnrollmentJpaEntity::toDomain)
                .toList();
    }
}
