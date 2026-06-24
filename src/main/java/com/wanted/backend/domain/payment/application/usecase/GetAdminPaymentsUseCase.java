package com.wanted.backend.domain.payment.application.usecase;

import com.wanted.backend.domain.payment.application.port.AdminPaymentQueryPort;
import com.wanted.backend.domain.payment.domain.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetAdminPaymentsUseCase {
    Page<AdminPaymentQueryPort.AdminPaymentData> handle(PaymentStatus status, String keyword, Pageable pageable);
}
