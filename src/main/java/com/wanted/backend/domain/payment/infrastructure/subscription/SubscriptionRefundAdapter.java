package com.wanted.backend.domain.payment.infrastructure.subscription;

import com.wanted.backend.domain.payment.application.port.PaymentByOrderQueryPort;
import com.wanted.backend.domain.payment.application.port.SubscriptionRefundPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubscriptionRefundAdapter implements SubscriptionRefundPort {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String CANCELLED_STATUS = "CANCELLED";

    private final SubscriptionRefundReferenceRepository repository;
    private final PaymentByOrderQueryPort paymentByOrderQueryPort;

    @Override
    @Transactional(readOnly = true)
    public Optional<SubscriptionData> findActiveByMemberId(Long memberId) {
        return repository.findByMemberIdAndStatus(memberId, ACTIVE_STATUS)
                .map(s -> {
                    PaymentByOrderQueryPort.PaymentDetail payment = paymentByOrderQueryPort
                            .findByOrderId(s.getOrderId())
                            .orElse(null);
                    return new SubscriptionData(
                            s.getId(),
                            s.getOrderId(),
                            s.getPlanId(),
                            payment != null ? payment.paidAt() : null,
                            s.getExpiredAt(),
                            payment != null ? payment.paidAmount() : 0,
                            s.getStatus()
                    );
                });
    }

    @Override
    @Transactional
    public void updateStatusToCancelled(Long subscriptionId) {
        repository.updateStatus(subscriptionId, CANCELLED_STATUS);
    }
}
