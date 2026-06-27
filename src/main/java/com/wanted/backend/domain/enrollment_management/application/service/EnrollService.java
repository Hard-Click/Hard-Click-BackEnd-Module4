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
        Instant now = Instant.now(clock);

        // 기존 수강 이력이 있으면: 활성 상태면 중복, 환불/만료 상태면 재활성화(재구매)
        return enrollmentRepository.findByMemberIdAndCourseId(command.memberId(), command.courseId())
                .map(existing -> {
                    if (existing.isActive()) {
                        throw new BusinessException(ErrorCode.ENROLLMENT_ALREADY_EXISTS);
                    }
                    existing.reactivate(now);
                    return enrollmentRepository.save(existing).getId();
                })
                .orElseGet(() -> enrollmentRepository.save(
                        Enrollment.create(command.memberId(), command.courseId(), now)).getId());
    }
}
