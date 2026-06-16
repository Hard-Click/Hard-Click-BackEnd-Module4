package com.wanted.backend.domain.payment.application.service;

import com.wanted.backend.domain.enrollment_management.domain.model.EnrollmentStatus;
import com.wanted.backend.domain.payment.application.command.CourseRefundCommand;
import com.wanted.backend.domain.payment.application.port.CourseProgressRatePort;
import com.wanted.backend.domain.payment.application.port.CourseRefundQueryPort;
import com.wanted.backend.domain.payment.application.port.EnrollmentRefundPort;
import com.wanted.backend.domain.payment.application.port.OrderStatusUpdatePort;
import com.wanted.backend.domain.payment.application.port.PaymentStatusUpdatePort;
import com.wanted.backend.domain.payment.application.usecase.RefundCourseUseCase;
import com.wanted.backend.domain.payment.domain.model.OrderStatus;
import com.wanted.backend.domain.payment.domain.model.PaymentStatus;
import com.wanted.backend.domain.payment.domain.model.Refund;
import com.wanted.backend.domain.payment.domain.repository.RefundRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefundCourseService implements RefundCourseUseCase {

    private static final int REFUND_PERIOD_DAYS = 7;
    private static final double REFUND_MAX_PROGRESS_RATE = 0.10;

    private final CourseRefundQueryPort courseRefundQueryPort;
    private final EnrollmentRefundPort enrollmentRefundPort;
    private final CourseProgressRatePort courseProgressRatePort;
    private final PaymentStatusUpdatePort paymentStatusUpdatePort;
    private final OrderStatusUpdatePort orderStatusUpdatePort;
    private final RefundRepository refundRepository;

    @Override
    @Transactional
    public Result handle(CourseRefundCommand command) {
        // 1. 수강 내역 조회 및 활성 상태 검증
        EnrollmentRefundPort.EnrollmentData enrollment =
                enrollmentRefundPort.findByMemberIdAndCourseId(command.memberId(), command.courseId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND_FOR_REFUND));

        if (enrollment.status() == EnrollmentStatus.REFUNDED) {
            throw new BusinessException(ErrorCode.ALREADY_REFUNDED);
        }
        if (enrollment.status() != EnrollmentStatus.IN_PROGRESS
                && enrollment.status() != EnrollmentStatus.ENROLLED) {
            throw new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND_FOR_REFUND);
        }

        // 2. 결제 내역 조회
        CourseRefundQueryPort.CoursePaymentData payment =
                courseRefundQueryPort.findByCourseIdAndMemberId(command.courseId(), command.memberId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (PaymentStatus.REFUNDED.name().equals(payment.paymentStatus())) {
            throw new BusinessException(ErrorCode.ALREADY_REFUNDED);
        }

        // 3. 7일 이내 환불 가능 기간 검증
        LocalDateTime refundDeadline = payment.paidAt().plusDays(REFUND_PERIOD_DAYS);
        if (LocalDateTime.now().isAfter(refundDeadline)) {
            throw new BusinessException(ErrorCode.REFUND_NOT_ELIGIBLE);
        }

        // 4. 진도율 10% 미만 검증
        double progressRate = courseProgressRatePort.getProgressRate(command.memberId(), command.courseId());
        if (progressRate >= REFUND_MAX_PROGRESS_RATE) {
            throw new BusinessException(ErrorCode.REFUND_NOT_ELIGIBLE);
        }

        // 5. 환불 처리
        Refund refund = Refund.createCourseRefund(
                command.memberId(),
                payment.paymentId(),
                enrollment.enrollmentId(),
                command.reason(),
                payment.paidAmount()
        );
        Refund saved = refundRepository.save(refund);

        paymentStatusUpdatePort.updateStatus(payment.paymentId(), PaymentStatus.REFUNDED.name());
        orderStatusUpdatePort.updateStatus(payment.orderId(), OrderStatus.CANCELLED);
        enrollmentRefundPort.updateStatus(enrollment.enrollmentId(), EnrollmentStatus.REFUNDED);

        return new Result(saved.getId(), payment.paymentId(), payment.paidAmount(), saved.getRefundedAt());
    }
}
