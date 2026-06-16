package com.wanted.backend.domain.cart.application.usecase;

public interface RemoveCartItemUseCase {
    void handle(Long memberId, Long courseId);
}
