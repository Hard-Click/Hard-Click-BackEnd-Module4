package com.wanted.backend.domain.wishlist.application.usecase;

import java.util.List;

public interface GetWishlistUseCase {

    List<Item> handle(Long memberId);

    record Item(
            Long courseId,
            String title,
            String instructorName,
            Integer price,
            Double averageRating,
            Integer reviewCount,
            boolean enrolled,
            boolean inCart
    ) {}
}
