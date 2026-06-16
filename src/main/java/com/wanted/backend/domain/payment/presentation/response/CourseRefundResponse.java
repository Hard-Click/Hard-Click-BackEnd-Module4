package com.wanted.backend.domain.payment.presentation.response;

import com.wanted.backend.domain.payment.application.usecase.RefundCourseUseCase;

import java.time.LocalDateTime;

public record CourseRefundResponse(
        Long refundId,
        Long paymentId,
        Integer refundAmount,
        LocalDateTime refundedAt
) {
    public static CourseRefundResponse from(RefundCourseUseCase.Result result) {
        return new CourseRefundResponse(
                result.refundId(),
                result.paymentId(),
                result.refundAmount(),
                result.refundedAt()
        );
    }
}
