package com.wanted.backend.domain.wishlist.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "wishlist_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WishlistItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_item_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    public static WishlistItemJpaEntity create(Long memberId, Long courseId) {
        WishlistItemJpaEntity e = new WishlistItemJpaEntity();
        e.memberId = memberId;
        e.courseId = courseId;
        e.addedAt = LocalDateTime.now();
        return e;
    }
}
