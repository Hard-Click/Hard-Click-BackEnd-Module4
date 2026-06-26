package com.wanted.backend.domain.cart.domain.repository;

import com.wanted.backend.domain.cart.domain.model.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartRepository {
    CartItem save(CartItem cartItem);
    List<CartItem> findAllByMemberId(Long memberId);
    Optional<CartItem> findByMemberIdAndCourseId(Long memberId, Long courseId);
    void deleteByMemberIdAndCourseId(Long memberId, Long courseId);
    void deleteAllByMemberId(Long memberId);
    boolean existsByMemberIdAndCourseId(Long memberId, Long courseId);
}
