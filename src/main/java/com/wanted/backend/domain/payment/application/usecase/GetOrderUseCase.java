package com.wanted.backend.domain.payment.application.usecase;

import com.wanted.backend.domain.payment.domain.model.PaymentType;

import java.util.List;

public interface GetOrderUseCase {
    Result handle(Long orderId, Long memberId);

    record Result(
            Long orderId,
            String orderNo,
            PaymentType paymentType,
            List<Item> items,
            Integer totalAmount
    ) {
        public record Item(Long courseId, String courseTitle, Integer price) {}
    }
}
