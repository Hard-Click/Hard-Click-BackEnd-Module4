package com.wanted.backend.domain.payment.application.port;

import com.wanted.backend.domain.payment.domain.model.OrderStatus;

public interface OrderStatusUpdatePort {

    void updateStatus(Long orderId, OrderStatus status);
}
