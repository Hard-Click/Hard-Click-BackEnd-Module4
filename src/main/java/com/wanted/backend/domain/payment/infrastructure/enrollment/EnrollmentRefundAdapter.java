package com.wanted.backend.domain.payment.infrastructure.enrollment;

import com.wanted.backend.domain.enrollment_management.domain.model.EnrollmentStatus;
import com.wanted.backend.domain.payment.application.port.EnrollmentRefundPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EnrollmentRefundAdapter implements EnrollmentRefundPort {

    private final EnrollmentRefundRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Optional<EnrollmentData> findByMemberIdAndCourseId(Long memberId, Long courseId) {
        return repository.findByMemberIdAndCourseId(memberId, courseId)
                .map(e -> new EnrollmentData(e.getId(), e.getMemberId(), e.getCourseId(), e.getEnrolledAt(), e.getStatus()));
    }

    @Override
    @Transactional
    public void updateStatus(Long enrollmentId, EnrollmentStatus status) {
        repository.updateStatus(enrollmentId, status);
    }
}
