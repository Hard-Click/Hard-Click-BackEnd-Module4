package com.wanted.backend.domain.enrollment_management.infrastructure.persistence;

import com.wanted.backend.domain.community.application.port.EnrollmentCompletedCheckPort;
import com.wanted.backend.domain.enrollment_management.domain.model.EnrollmentStatus;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentCompletedCheckAdapter implements EnrollmentCompletedCheckPort {

    private final SpringDataEnrollmentRepository repository;

    public EnrollmentCompletedCheckAdapter(SpringDataEnrollmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isCompleted(Long memberId, Long courseId) {
        return repository.existsByMemberIdAndCourseIdAndStatus(memberId, courseId, EnrollmentStatus.COMPLETED);
    }
}