package com.wanted.backend.domain.payment.application.service;

import com.wanted.backend.domain.payment.application.command.SubscriptionRefundCommand;
import com.wanted.backend.domain.payment.application.port.OrderStatusUpdatePort;
import com.wanted.backend.domain.payment.application.port.PaymentRefundQueryPort;
import com.wanted.backend.domain.payment.application.port.PaymentStatusUpdatePort;
import com.wanted.backend.domain.payment.application.port.SubscriptionRefundPort;
import com.wanted.backend.domain.payment.application.usecase.RefundSubscriptionUseCase;
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
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class RefundSubscriptionService implements RefundSubscriptionUseCase {

    private final SubscriptionRefundPort subscriptionRefundPort;
    private final PaymentRefundQueryPort paymentRefundQueryPort;
    private final PaymentStatusUpdatePort paymentStatusUpdatePort;
    private final OrderStatusUpdatePort orderStatusUpdatePort;
    private final RefundRepository refundRepository;

    @Override
    @Transactional
    public Result handle(SubscriptionRefundCommand command) {
        // 1. 활성 구독 조회
        SubscriptionRefundPort.SubscriptionData subscription =
                subscriptionRefundPort.findActiveByMemberId(command.memberId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        // 2. ACTIVE 상태 검증
        if (!"ACTIVE".equals(subscription.status())) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_NOT_ACTIVE);
        }

        // 3. 만료된 구독 환불 불가
        LocalDateTime now = LocalDateTime.now();
        long remainingDays = ChronoUnit.DAYS.between(now, subscription.expiredAt());
        long totalDays = ChronoUnit.DAYS.between(subscription.startedAt(), subscription.expiredAt());

        if (remainingDays <= 0 || totalDays <= 0) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_NOT_ACTIVE);
        }

        // 4. 일할 환불 금액 계산
        int refundAmount = (int) Math.floor((double) remainingDays / totalDays * subscription.paidAmount());

        // 5. 연결된 결제 내역 조회 (orderId → paymentId)
        if (subscription.orderId() == null) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }
        Long paymentId = paymentRefundQueryPort.findPaymentIdByOrderId(subscription.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 6. 환불 처리
        Refund refund = Refund.createSubscriptionRefund(
                command.memberId(),
                paymentId,
                subscription.subscriptionId(),
                command.reason(),
                refundAmount
        );
        Refund saved = refundRepository.save(refund);

        paymentStatusUpdatePort.updateStatus(paymentId, PaymentStatus.REFUNDED.name());
        orderStatusUpdatePort.updateStatus(subscription.orderId(), OrderStatus.CANCELLED);
        subscriptionRefundPort.updateStatusToCancelled(subscription.subscriptionId());

        return new Result(saved.getId(), subscription.subscriptionId(), refundAmount, saved.getRefundedAt());
    }
}
