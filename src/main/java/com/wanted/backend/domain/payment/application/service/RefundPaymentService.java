package com.wanted.backend.domain.payment.application.service;

import com.wanted.backend.domain.payment.application.port.EnrollmentRevocationPort;
import com.wanted.backend.domain.payment.application.port.PgClient;
import com.wanted.backend.domain.payment.application.usecase.RefundPaymentUseCase;
import com.wanted.backend.domain.payment.domain.model.Payment;
import com.wanted.backend.domain.payment.domain.repository.PaymentRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefundPaymentService implements RefundPaymentUseCase {

    private static final String CANCEL_REASON = "관리자 환불 처리";

    private final PaymentRepository paymentRepository;
    private final EnrollmentRevocationPort enrollmentRevocationPort;
    private final PgClient pgClient;

    @Override
    public void handle(Long paymentId) {
        // Step 1: 결제 조회 (pgTransactionId = Toss paymentKey)
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getPgTransactionId() == null) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_REFUNDABLE);
        }

        // Step 2: PG 취소 — DB 업데이트 전 실행 (TX 밖)
        try {
            pgClient.cancel(payment.getPgTransactionId(), payment.getAmount(), CANCEL_REASON);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.PG_TIMEOUT, e);
        }

        // Step 3: DB 상태 갱신 + 수강 권한 박탈
        Payment refunded = paymentRepository.refundPayment(paymentId);
        if (refunded.getCourseId() != null) {
            enrollmentRevocationPort.revoke(refunded.getMemberId(), refunded.getCourseId());
        }
    }
}
