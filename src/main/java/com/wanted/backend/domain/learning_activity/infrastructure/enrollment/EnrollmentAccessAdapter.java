package com.wanted.backend.domain.learning_activity.infrastructure.enrollment;

import com.wanted.backend.domain.enrollment_management.domain.model.EnrollmentStatus;
import com.wanted.backend.domain.learning_activity.application.port.EnrollmentAccessPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentAccessAdapter implements EnrollmentAccessPort {

    private static final Set<EnrollmentStatus> ACTIVE_ENROLLMENT_STATUSES =
            EnumSet.of(EnrollmentStatus.IN_PROGRESS, EnrollmentStatus.COMPLETED);

    private final SpringDataEnrollmentAccessRepository repository;

    @Override
    public boolean hasActiveEnrollment(Long memberId, Long courseId) {
        return repository.existsByMemberIdAndCourseIdAndStatusInAndExpiredAtIsNull(
                memberId,
                courseId,
                ACTIVE_ENROLLMENT_STATUSES
        ) || repository.existsByMemberIdAndCourseIdAndStatusInAndExpiredAtGreaterThanEqual(
                memberId,
                courseId,
                ACTIVE_ENROLLMENT_STATUSES,
                LocalDateTime.now()
        );
    }
}
