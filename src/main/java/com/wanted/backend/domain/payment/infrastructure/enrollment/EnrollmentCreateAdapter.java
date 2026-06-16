package com.wanted.backend.domain.payment.infrastructure.enrollment;

import com.wanted.backend.domain.payment.application.port.EnrollmentCreatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EnrollmentCreateAdapter implements EnrollmentCreatePort {

    private final WritableEnrollmentJpaRepository repository;

    @Override
    public void createAll(Long memberId, List<Long> courseIds) {
        List<WritableEnrollmentJpaEntity> enrollments = courseIds.stream()
                .filter(courseId -> !repository.existsByMemberIdAndCourseId(memberId, courseId))
                .map(courseId -> WritableEnrollmentJpaEntity.create(memberId, courseId))
                .toList();

        if (!enrollments.isEmpty()) {
            repository.saveAll(enrollments);
        }
    }
}
