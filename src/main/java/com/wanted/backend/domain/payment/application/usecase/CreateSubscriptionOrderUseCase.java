package com.wanted.backend.domain.payment.application.usecase;

import com.wanted.backend.domain.payment.application.command.CreateSubscriptionOrderCommand;

public interface CreateSubscriptionOrderUseCase {
    Result handle(CreateSubscriptionOrderCommand command);

    record Result(Long orderId, String orderNo, String planName, Integer amount) {}
}
