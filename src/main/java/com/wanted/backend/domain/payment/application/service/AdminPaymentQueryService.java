package com.wanted.backend.domain.payment.application.service;

import com.wanted.backend.domain.payment.application.port.AdminPaymentQueryPort;
import com.wanted.backend.domain.payment.application.usecase.GetAdminPaymentsUseCase;
import com.wanted.backend.domain.payment.domain.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPaymentQueryService implements GetAdminPaymentsUseCase {

    private final AdminPaymentQueryPort adminPaymentQueryPort;

    @Override
    public Page<AdminPaymentQueryPort.AdminPaymentData> handle(PaymentStatus status, String keyword, Pageable pageable) {
        return adminPaymentQueryPort.search(status, keyword, pageable);
    }
}
