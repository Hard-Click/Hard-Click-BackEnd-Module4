package com.wanted.backend.domain.cart.application.service;

import com.wanted.backend.domain.cart.application.usecase.RemoveCartItemUseCase;
import com.wanted.backend.domain.cart.domain.repository.CartRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveCartItemService implements RemoveCartItemUseCase {

    private final CartRepository cartRepository;

    @Override
    @Transactional
    public void handle(Long memberId, Long courseId) {
        if (!cartRepository.existsByMemberIdAndCourseId(memberId, courseId)) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        cartRepository.deleteByMemberIdAndCourseId(memberId, courseId);
    }
}
