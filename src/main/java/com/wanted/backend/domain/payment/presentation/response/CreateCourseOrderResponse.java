package com.wanted.backend.domain.payment.presentation.response;

import com.wanted.backend.domain.payment.application.usecase.CreateCourseOrderUseCase;

import java.util.List;

public record CreateCourseOrderResponse(
        Long orderId,
        String orderNo,
        List<Item> items,
        Integer totalAmount
) {
    public record Item(Long courseId, String courseTitle, Integer price) {}

    public static CreateCourseOrderResponse from(CreateCourseOrderUseCase.Result result) {
        List<Item> items = result.items().stream()
                .map(i -> new Item(i.courseId(), i.courseTitle(), i.price()))
                .toList();
        return new CreateCourseOrderResponse(result.orderId(), result.orderNo(), items, result.totalAmount());
    }
}
