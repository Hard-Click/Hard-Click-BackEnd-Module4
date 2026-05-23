package com.wanted.backend.domain.enrollment_management.application.usecase;

import com.wanted.backend.domain.enrollment_management.application.dto.MyEnrollmentResult;

import java.util.List;

public interface GetMyEnrollmentsUseCase {
    List<MyEnrollmentResult> handle(Long userId, String statusFilter);
}
