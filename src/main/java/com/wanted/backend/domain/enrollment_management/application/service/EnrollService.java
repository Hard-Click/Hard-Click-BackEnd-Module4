package com.wanted.backend.domain.enrollment_management.application.service;

import com.wanted.backend.domain.enrollment_management.application.command.EnrollCommand;
import com.wanted.backend.domain.enrollment_management.application.usecase.EnrollUseCase;
import com.wanted.backend.domain.enrollment_management.domain.model.Enrollment;
import com.wanted.backend.domain.enrollment_management.domain.repository.EnrollmentRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollService implements EnrollUseCase {

    private final EnrollmentRepository enrollmentRepository;
    private final Clock clock;

    @Override
    public Long handle(EnrollCommand command) {
        // 중복 수강신청 검증
        if (enrollmentRepository.existsByUserIdAndCourseId(command.userId(), command.courseId())) {
            throw new BusinessException(ErrorCode.ENROLLMENT_ALREADY_EXISTS);
        }

        Enrollment enrollment = Enrollment.create(command.userId(), command.courseId(), Instant.now(clock));
        return enrollmentRepository.save(enrollment).getId();
    }
}
