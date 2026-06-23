package com.wanted.backend.domain.wishlist.domain.model;

import java.time.LocalDateTime;

public class WishlistItem {

    private Long id;
    private Long memberId;
    private Long courseId;
    private LocalDateTime addedAt;

    private WishlistItem() {}

    public static WishlistItem create(Long memberId, Long courseId) {
        WishlistItem item = new WishlistItem();
        item.memberId = memberId;
        item.courseId = courseId;
        item.addedAt = LocalDateTime.now();
        return item;
    }

    public static WishlistItem restore(Long id, Long memberId, Long courseId, LocalDateTime addedAt) {
        WishlistItem item = new WishlistItem();
        item.id = id;
        item.memberId = memberId;
        item.courseId = courseId;
        item.addedAt = addedAt;
        return item;
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public Long getCourseId() { return courseId; }
    public LocalDateTime getAddedAt() { return addedAt; }
}
