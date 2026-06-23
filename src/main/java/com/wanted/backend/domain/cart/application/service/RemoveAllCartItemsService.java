package com.wanted.backend.domain.cart.application.service;

import com.wanted.backend.domain.cart.application.usecase.RemoveAllCartItemsUseCase;
import com.wanted.backend.domain.cart.domain.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveAllCartItemsService implements RemoveAllCartItemsUseCase {

    private final CartRepository cartRepository;

    @Override
    @Transactional
    public void handle(Long memberId) {
        cartRepository.deleteAllByMemberId(memberId);
    }
}
