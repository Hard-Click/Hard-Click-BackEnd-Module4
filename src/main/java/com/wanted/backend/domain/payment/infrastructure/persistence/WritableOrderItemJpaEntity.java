package com.wanted.backend.domain.payment.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "WritableOrderItem")
@Getter
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WritableOrderItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "course_title", nullable = false)
    private String courseTitle;

    @Column(nullable = false)
    private Integer price;

    public static WritableOrderItemJpaEntity create(Long orderId, Long courseId, String courseTitle, Integer price) {
        WritableOrderItemJpaEntity e = new WritableOrderItemJpaEntity();
        e.orderId = orderId;
        e.courseId = courseId;
        e.courseTitle = courseTitle;
        e.price = price;
        return e;
    }
}
