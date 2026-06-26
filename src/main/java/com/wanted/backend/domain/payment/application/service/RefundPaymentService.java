package com.wanted.backend.domain.payment.application.service;

import com.wanted.backend.domain.payment.application.port.EnrollmentRevocationPort;
import com.wanted.backend.domain.payment.application.usecase.RefundPaymentUseCase;
import com.wanted.backend.domain.payment.domain.model.Payment;
import com.wanted.backend.domain.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RefundPaymentService implements RefundPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final EnrollmentRevocationPort enrollmentRevocationPort;

    @Override
    public void handle(Long paymentId) {
        Payment payment = paymentRepository.refundPayment(paymentId);
        if (payment.getCourseId() != null) {
            enrollmentRevocationPort.revoke(payment.getMemberId(), payment.getCourseId());
        }
    }
}
