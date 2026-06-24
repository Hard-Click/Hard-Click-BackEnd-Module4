package com.wanted.backend.domain.wishlist.infrastructure.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Immutable
@Entity(name = "WishlistCartCheck")
@Getter
@Table(name = "cart_items")
public class WishlistCartCheckEntity {

    @Id
    @Column(name = "cart_item_id")
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "course_id")
    private Long courseId;
}
