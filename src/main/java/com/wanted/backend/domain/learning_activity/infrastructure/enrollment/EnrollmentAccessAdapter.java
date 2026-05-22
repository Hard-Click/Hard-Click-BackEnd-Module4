package com.wanted.backend.domain.learning_activity.infrastructure.enrollment;

import com.wanted.backend.domain.learning_activity.application.port.EnrollmentAccessPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentAccessAdapter implements EnrollmentAccessPort {

    private final SpringDataEnrollmentAccessRepository repository;

    @Override
    public boolean hasActiveEnrollment(Long memberId, Long courseId) {
        return repository.existsActiveEnrollment(memberId, courseId);
    }
}
