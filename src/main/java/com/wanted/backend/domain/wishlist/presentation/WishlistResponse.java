package com.wanted.backend.domain.wishlist.presentation;

import com.wanted.backend.domain.wishlist.application.usecase.GetWishlistUseCase;

import java.util.List;

public record WishlistResponse(
        List<Item> items,
        int totalCount
) {
    public record Item(
            Long courseId,
            String title,
            String instructorName,
            Integer price,
            Double averageRating,
            Integer reviewCount,
            boolean enrolled,
            boolean inCart
    ) {}

    public static WishlistResponse from(List<GetWishlistUseCase.Item> useCaseItems) {
        List<Item> items = useCaseItems.stream()
                .map(i -> new Item(
                        i.courseId(),
                        i.title(),
                        i.instructorName(),
                        i.price(),
                        i.averageRating(),
                        i.reviewCount(),
                        i.enrolled(),
                        i.inCart()
                ))
                .toList();
        return new WishlistResponse(items, items.size());
    }
}
