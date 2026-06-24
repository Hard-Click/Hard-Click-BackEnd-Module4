package com.wanted.backend.domain.order.application.usecase;

import com.wanted.backend.domain.order.application.dto.OrderDetailResult;

public interface GetOrderUseCase {

    OrderDetailResult getOrder(Long memberId, Long orderId);
}
