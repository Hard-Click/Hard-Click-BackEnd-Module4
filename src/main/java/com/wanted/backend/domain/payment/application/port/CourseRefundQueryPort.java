package com.wanted.backend.domain.payment.application.port;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CourseRefundQueryPort {

    /**
     * 회원이 특정 강의에 대해 결제한 내역을 조회한다.
     * orders → order_items(courseId) → payments 조인.
     */
    Optional<CoursePaymentData> findByCourseIdAndMemberId(Long courseId, Long memberId);

    record CoursePaymentData(
            Long paymentId,
            Long orderId,
            LocalDateTime paidAt,
            Integer paidAmount,
            String paymentStatus
    ) {}
}
