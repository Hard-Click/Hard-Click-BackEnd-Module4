package com.wanted.backend.domain.payment.presentation.response;

import com.wanted.backend.domain.payment.application.usecase.GetOrderUseCase;
import com.wanted.backend.domain.payment.domain.model.PaymentType;

import java.util.List;

public record GetOrderResponse(
        Long orderId,
        String orderNo,
        PaymentType paymentType,
        List<Item> items,
        Integer totalAmount
) {
    public record Item(Long courseId, String courseTitle, Integer price) {}

    public static GetOrderResponse from(GetOrderUseCase.Result result) {
        List<Item> items = result.items().stream()
                .map(i -> new Item(i.courseId(), i.courseTitle(), i.price()))
                .toList();
        return new GetOrderResponse(result.orderId(), result.orderNo(), result.paymentType(),
                items, result.totalAmount());
    }
}
