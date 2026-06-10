package com.wanted.backend.domain.enrollment_management.application.service;

import com.wanted.backend.domain.enrollment_management.application.dto.MyEnrollmentResult;
import com.wanted.backend.domain.enrollment_management.application.port.CourseInfoQueryPort;
import com.wanted.backend.domain.enrollment_management.application.usecase.GetMyEnrollmentsUseCase;
import com.wanted.backend.domain.enrollment_management.domain.model.Enrollment;
import com.wanted.backend.domain.enrollment_management.domain.model.EnrollmentStatus;
import com.wanted.backend.domain.enrollment_management.domain.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyEnrollmentsService implements GetMyEnrollmentsUseCase {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseInfoQueryPort courseInfoQueryPort;

    @Override
    public List<MyEnrollmentResult> handle(Long memberId, String statusFilter) {
        List<Enrollment> enrollments = fetchEnrollments(memberId, statusFilter);

        return enrollments.stream()
                .map(e -> new MyEnrollmentResult(
                        e.getId(),
                        e.getCourseId(),
                        courseInfoQueryPort.getCourseTitle(e.getCourseId()),
                        e.getEffectiveStatus(),
                        e.getEnrolledAt(),
                        e.getExpiredAt(),
                        0  // 진행률은 learning_activity 도메인 연동 전까지 0
                ))
                .toList();
    }

    private List<Enrollment> fetchEnrollments(Long memberId, String statusFilter) {
        if ("ALL".equalsIgnoreCase(statusFilter)) {
            return enrollmentRepository.findByMemberId(memberId);
        }
        EnrollmentStatus status = EnrollmentStatus.valueOf(statusFilter.toUpperCase());
        return enrollmentRepository.findByMemberIdAndStatus(memberId, status);
    }
}
