package com.wanted.backend.domain.cart.domain.model;

import java.time.LocalDateTime;

public class CartItem {

    private Long id;
    private Long memberId;
    private Long courseId;
    private LocalDateTime addedAt;

    private CartItem() {}

    public static CartItem create(Long memberId, Long courseId) {
        CartItem item = new CartItem();
        item.memberId = memberId;
        item.courseId = courseId;
        item.addedAt = LocalDateTime.now();
        return item;
    }

    public static CartItem restore(Long id, Long memberId, Long courseId, LocalDateTime addedAt) {
        CartItem item = new CartItem();
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
