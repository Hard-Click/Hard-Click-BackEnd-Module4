package com.wanted.backend.domain.order.application.dto;

import com.wanted.backend.domain.order.domain.model.OrderStatus;
import com.wanted.backend.domain.order.domain.model.OrderType;

import java.util.List;

public record CheckoutResult(
        String orderNo,
        OrderType type,
        OrderStatus status,
        List<Item> items,
        int totalAmount,
        int finalAmount
) {
    public record Item(Long courseId, String title, int price) {}
}
