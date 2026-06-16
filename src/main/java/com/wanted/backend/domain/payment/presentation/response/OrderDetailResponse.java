package com.wanted.backend.domain.payment.presentation.response;

import com.wanted.backend.domain.payment.application.usecase.GetOrderDetailUseCase;
import com.wanted.backend.domain.payment.domain.model.OrderStatus;
import com.wanted.backend.domain.payment.domain.model.PaymentType;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long orderId,
        String orderNo,
        OrderStatus orderStatus,
        PaymentType paymentType,
        LocalDateTime paidAt,
        List<Item> items,
        Integer totalAmount,
        RefundInfo refundInfo
) {
    public record Item(
            Long courseId,
            String title,
            Integer price,
            double progressRate,
            long daysElapsed,
            boolean refundEligible
    ) {}

    public record RefundInfo(
            boolean refundable,
            List<RefundItem> eligibleItems,
            Integer totalRefundAmount
    ) {}

    public record RefundItem(Long courseId, String title, Integer refundAmount) {}

    public static OrderDetailResponse from(GetOrderDetailUseCase.Result result) {
        List<Item> items = result.items().stream()
                .map(i -> new Item(i.courseId(), i.title(), i.price(),
                        i.progressRate(), i.daysElapsed(), i.refundEligible()))
                .toList();

        List<RefundItem> eligibleItems = result.refundInfo().eligibleItems().stream()
                .map(i -> new RefundItem(i.courseId(), i.title(), i.refundAmount()))
                .toList();

        RefundInfo refundInfo = new RefundInfo(
                result.refundInfo().refundable(),
                eligibleItems,
                result.refundInfo().totalRefundAmount()
        );

        return new OrderDetailResponse(
                result.orderId(), result.orderNo(), result.orderStatus(),
                result.paymentType(), result.paidAt(), items,
                result.totalAmount(), refundInfo
        );
    }
}
