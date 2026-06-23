package com.wanted.backend.domain.cart.infrastructure.persistence;

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
@Table(name = "cart_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    public static CartItemJpaEntity create(Long memberId, Long courseId) {
        CartItemJpaEntity e = new CartItemJpaEntity();
        e.memberId = memberId;
        e.courseId = courseId;
        e.addedAt = LocalDateTime.now();
        return e;
    }
}
