package com.wanted.backend.domain.cart.application.service;

import com.wanted.backend.domain.cart.application.usecase.AddCartItemUseCase;
import com.wanted.backend.domain.cart.domain.model.CartItem;
import com.wanted.backend.domain.cart.domain.repository.CartRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddCartItemService implements AddCartItemUseCase {

    private final CartRepository cartRepository;

    @Override
    @Transactional
    public Result handle(Long memberId, Long courseId) {
        if (cartRepository.existsByMemberIdAndCourseId(memberId, courseId)) {
            throw new BusinessException(ErrorCode.CART_ITEM_ALREADY_EXISTS);
        }

        CartItem saved = cartRepository.save(CartItem.create(memberId, courseId));
        return new Result(saved.getId(), saved.getCourseId());
    }
}
