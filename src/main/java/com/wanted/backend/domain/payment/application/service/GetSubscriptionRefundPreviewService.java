package com.wanted.backend.domain.payment.application.service;

import com.wanted.backend.domain.payment.application.port.SubscriptionRefundPort;
import com.wanted.backend.domain.payment.application.usecase.GetSubscriptionRefundPreviewUseCase;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetSubscriptionRefundPreviewService implements GetSubscriptionRefundPreviewUseCase {

    private final SubscriptionRefundPort subscriptionRefundPort;

    @Override
    public Preview handle(Long memberId) {
        SubscriptionRefundPort.SubscriptionData subscription =
                subscriptionRefundPort.findActiveByMemberId(memberId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        if (!"ACTIVE".equals(subscription.status())) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_NOT_ACTIVE);
        }

        LocalDateTime now = LocalDateTime.now();
        long remainingDays = ChronoUnit.DAYS.between(now, subscription.expiredAt());
        long totalDays = ChronoUnit.DAYS.between(subscription.startedAt(), subscription.expiredAt());

        if (remainingDays <= 0 || totalDays <= 0) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_NOT_ACTIVE);
        }

        int refundAmount = calculateRefundAmount(remainingDays, totalDays, subscription.paidAmount());

        return new Preview(
                subscription.subscriptionId(),
                subscription.paidAmount(),
                refundAmount,
                remainingDays,
                totalDays,
                subscription.expiredAt()
        );
    }

    private int calculateRefundAmount(long remainingDays, long totalDays, int paidAmount) {
        return (int) Math.floor((double) remainingDays / totalDays * paidAmount);
    }
}
