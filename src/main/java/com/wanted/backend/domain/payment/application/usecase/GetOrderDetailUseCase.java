package com.wanted.backend.domain.payment.application.usecase;

import com.wanted.backend.domain.payment.domain.model.OrderStatus;
import com.wanted.backend.domain.payment.domain.model.PaymentType;

import java.time.LocalDateTime;
import java.util.List;

public interface GetOrderDetailUseCase {

    Result handle(Long orderId, Long memberId);

    record Result(
            Long orderId,
            String orderNo,
            OrderStatus orderStatus,
            PaymentType paymentType,
            LocalDateTime paidAt,
            List<ItemResult> items,
            Integer totalAmount,
            RefundInfo refundInfo
    ) {}

    record ItemResult(
            Long courseId,
            String title,
            Integer price,
            double progressRate,
            long daysElapsed,
            boolean refundEligible
    ) {}

    record RefundInfo(
            boolean refundable,
            List<RefundItem> eligibleItems,
            Integer totalRefundAmount
    ) {}

    record RefundItem(
            Long courseId,
            String title,
            Integer refundAmount
    ) {}
}
