package com.wanted.backend.domain.order.infrastructure.persistence;

import com.wanted.backend.domain.order.domain.model.OrderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 주문 쓰기 전용 엔티티.
 * payment 도메인의 읽기 전용 OrderJpaEntity와 동일한 orders 테이블을 매핑하므로
 * JPA 엔티티명 충돌을 피하기 위해 클래스명을 OrderEntity로 둔다.
 */
@Entity
@Getter
@Table(name = "orders", uniqueConstraints = {
        @UniqueConstraint(name = "uk_orders_order_no", columnNames = "order_no")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity {

    // 기존 orders 테이블의 order_id 컬럼에 AUTO_INCREMENT가 없어(읽기 전용 immutable 엔티티가 먼저 생성)
    // 앱이 id를 채번하는 TABLE 전략 사용. 스키마 변경 없이 기존 테이블에 INSERT 가능.
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "orders_id_gen")
    @TableGenerator(
            name = "orders_id_gen",
            table = "id_sequences",
            pkColumnName = "seq_name",
            valueColumnName = "seq_value",
            pkColumnValue = "orders",
            allocationSize = 50
    )
    @Column(name = "order_id")
    private Long id;

    @Column(name = "order_no", nullable = false)
    private String orderNo;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private OrderType type;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "final_amount", nullable = false)
    private Integer finalAmount;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public OrderEntity(String orderNo, Long memberId, OrderType type, String status,
                       Integer totalAmount, Integer finalAmount, LocalDateTime orderedAt, LocalDateTime paidAt) {
        this.orderNo = orderNo;
        this.memberId = memberId;
        this.type = type;
        this.status = status;
        this.totalAmount = totalAmount;
        this.finalAmount = finalAmount;
        this.orderedAt = orderedAt;
        this.paidAt = paidAt;
    }
}
