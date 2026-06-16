package com.wanted.backend.domain.payment.application.usecase;

import com.wanted.backend.domain.payment.application.command.CourseRefundCommand;

import java.time.LocalDateTime;

public interface RefundCourseUseCase {

    Result handle(CourseRefundCommand command);

    record Result(
            Long refundId,
            Long paymentId,
            Integer refundAmount,
            LocalDateTime refundedAt
    ) {}
}
