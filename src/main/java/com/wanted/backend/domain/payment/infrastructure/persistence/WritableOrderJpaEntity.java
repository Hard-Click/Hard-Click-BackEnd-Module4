package com.wanted.backend.domain.payment.infrastructure.persistence;

import com.wanted.backend.domain.payment.domain.model.OrderStatus;
import com.wanted.backend.domain.payment.domain.model.PaymentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "WritableOrder")
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WritableOrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "order_no", nullable = false, unique = true)
    private String orderNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public static WritableOrderJpaEntity create(Long memberId, String orderNo, PaymentType paymentType, Long planId) {
        WritableOrderJpaEntity e = new WritableOrderJpaEntity();
        e.memberId = memberId;
        e.orderNo = orderNo;
        e.paymentType = paymentType;
        e.status = OrderStatus.PENDING;
        e.planId = planId;
        e.createdAt = LocalDateTime.now();
        return e;
    }
}
