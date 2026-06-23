package com.wanted.backend.domain.cart.application.usecase;

public interface AddCartItemUseCase {

    Result handle(Long memberId, Long courseId);

    record Result(Long cartItemId, Long courseId) {}
}
