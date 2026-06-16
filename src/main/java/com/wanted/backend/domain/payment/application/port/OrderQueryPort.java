package com.wanted.backend.domain.payment.application.port;

import com.wanted.backend.domain.payment.domain.model.OrderStatus;
import com.wanted.backend.domain.payment.domain.model.PaymentType;

import java.util.List;

public interface OrderQueryPort {
    OrderData findById(Long orderId);
    OrderData findByOrderNo(String orderNo);

    record OrderData(
            Long orderId,
            String orderNo,
            PaymentType paymentType,
            OrderStatus status,
            Long planId,
            List<OrderItemData> items
    ) {}

    record OrderItemData(Long courseId, String courseTitle, Integer price) {}
}
