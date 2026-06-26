package com.wanted.backend.domain.wishlist.domain.repository;

import com.wanted.backend.domain.wishlist.domain.model.WishlistItem;

import java.util.List;

public interface WishlistRepository {
    WishlistItem save(WishlistItem item);
    List<WishlistItem> findAllByMemberId(Long memberId);
    void deleteByMemberIdAndCourseId(Long memberId, Long courseId);
    boolean existsByMemberIdAndCourseId(Long memberId, Long courseId);
}
