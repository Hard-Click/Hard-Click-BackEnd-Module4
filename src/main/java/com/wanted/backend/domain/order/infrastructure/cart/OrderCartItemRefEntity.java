package com.wanted.backend.domain.order.infrastructure.cart;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity(name = "OrderCartItemRef")
@Getter
@Immutable
@Table(name = "cart_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderCartItemRefEntity {

    @Id
    @Column(name = "cart_item_id")
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "course_id")
    private Long courseId;
}
