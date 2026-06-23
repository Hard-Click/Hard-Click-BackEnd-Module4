package com.wanted.backend.domain.wishlist.application.usecase;

import java.util.List;

public interface GetWishlistUseCase {

    List<Item> handle(Long memberId);

    record Item(
            Long courseId,
            String title,
            String subject,
            String thumbnailUrl,
            String priceType,
            String instructorName,
            Integer price,
            Double averageRating,
            Integer reviewCount,
            Integer enrollmentCount,
            boolean enrolled,
            boolean inCart
    ) {}
}
