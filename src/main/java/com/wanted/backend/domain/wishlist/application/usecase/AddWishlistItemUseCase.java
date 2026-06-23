package com.wanted.backend.domain.wishlist.application.usecase;

public interface AddWishlistItemUseCase {
    void handle(Long memberId, Long courseId);
}
