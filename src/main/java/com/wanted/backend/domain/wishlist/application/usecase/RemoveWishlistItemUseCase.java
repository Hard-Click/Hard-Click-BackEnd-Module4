package com.wanted.backend.domain.wishlist.application.usecase;

public interface RemoveWishlistItemUseCase {
    void handle(Long memberId, Long courseId);
}
