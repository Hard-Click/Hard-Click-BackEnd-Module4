package com.wanted.backend.domain.cart.application.usecase;

import java.util.List;

public interface GetCartUseCase {

    Result handle(Long memberId);

    record Result(
            List<Item> items,
            int selectedCount,
            Integer totalAmount
    ) {}

    record Item(
            Long courseId,
            String title,
            String instructorName,
            Integer price
    ) {}
}
