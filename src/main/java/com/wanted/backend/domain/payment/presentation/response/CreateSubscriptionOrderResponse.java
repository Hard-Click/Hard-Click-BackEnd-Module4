package com.wanted.backend.domain.payment.presentation.response;

import com.wanted.backend.domain.payment.application.usecase.CreateSubscriptionOrderUseCase;

public record CreateSubscriptionOrderResponse(
        Long orderId,
        String orderNo,
        String planName,
        Integer amount
) {
    public static CreateSubscriptionOrderResponse from(CreateSubscriptionOrderUseCase.Result result) {
        return new CreateSubscriptionOrderResponse(
                result.orderId(), result.orderNo(), result.planName(), result.amount());
    }
}
