package com.wanted.backend.domain.order.application.dto;

import com.wanted.backend.domain.order.domain.model.OrderStatus;
import com.wanted.backend.domain.order.domain.model.OrderType;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResult(
        String orderNo,
        OrderStatus status,
        OrderType paymentType,
        LocalDateTime orderedAt,
        LocalDateTime paidAt,
        List<Item> items,
        int totalAmount
) {
    public record Item(
            Long courseId,
            String title,
            int price,
            boolean refundable,
            int refundAmount,
            String enrollStatus
    ) {}
}
