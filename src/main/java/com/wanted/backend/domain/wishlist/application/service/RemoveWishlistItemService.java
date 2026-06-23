package com.wanted.backend.domain.wishlist.application.service;

import com.wanted.backend.domain.wishlist.application.usecase.RemoveWishlistItemUseCase;
import com.wanted.backend.domain.wishlist.domain.repository.WishlistRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveWishlistItemService implements RemoveWishlistItemUseCase {

    private final WishlistRepository wishlistRepository;

    @Override
    @Transactional
    public void handle(Long memberId, Long courseId) {
        if (!wishlistRepository.existsByMemberIdAndCourseId(memberId, courseId)) {
            throw new BusinessException(ErrorCode.WISHLIST_ITEM_NOT_FOUND);
        }
        wishlistRepository.deleteByMemberIdAndCourseId(memberId, courseId);
    }
}
