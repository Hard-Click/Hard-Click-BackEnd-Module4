package com.wanted.backend.domain.cart.presentation.response;

import com.wanted.backend.domain.cart.application.usecase.GetCartUseCase;

import java.util.List;

public record CartResponse(
        List<Item> items,
        int selectedCount,
        Integer totalAmount
) {
    public record Item(
            Long courseId,
            String title,
            String instructorName,
            Integer price
    ) {}

    public static CartResponse from(GetCartUseCase.Result result) {
        List<Item> items = result.items().stream()
                .map(i -> new Item(i.courseId(), i.title(), i.instructorName(), i.price()))
                .toList();
        return new CartResponse(items, result.selectedCount(), result.totalAmount());
    }
}
