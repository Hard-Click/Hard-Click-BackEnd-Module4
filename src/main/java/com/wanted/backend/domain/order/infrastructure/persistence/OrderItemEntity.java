package com.wanted.backend.domain.order.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 항목 쓰기 전용 엔티티 (order_items 테이블).
 * payment 도메인의 읽기 전용 OrderItemJpaEntity와 충돌을 피하려 OrderItemEntity로 명명.
 * COURSE 주문 항목만 영속화한다(course_id NOT NULL).
 */
@Entity
@Getter
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_items_order_id", columnList = "order_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemEntity {

    // order_items.order_item_id 컬럼도 AUTO_INCREMENT가 없어 TABLE 전략으로 채번한다.
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "order_items_id_gen")
    @TableGenerator(
            name = "order_items_id_gen",
            table = "id_sequences",
            pkColumnName = "seq_name",
            valueColumnName = "seq_value",
            pkColumnValue = "order_items",
            allocationSize = 50
    )
    @Column(name = "order_item_id")
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "title")
    private String title;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "refunded", nullable = false)
    private boolean refunded;

    public OrderItemEntity(Long orderId, Long courseId, String title, Integer price) {
        this.orderId = orderId;
        this.courseId = courseId;
        this.title = title;
        this.price = price;
        this.refunded = false;
    }

    public void markRefunded() {
        this.refunded = true;
    }
}
