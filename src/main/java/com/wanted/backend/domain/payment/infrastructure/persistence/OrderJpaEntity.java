package com.wanted.backend.domain.payment.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Getter
@Immutable
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderJpaEntity {

    @Id
    @Column(name = "order_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "order_no", nullable = false)
    private String orderNo;

    @Column(name = "payment_type", nullable = false)
    private String paymentType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "final_amount", nullable = false)
    private Integer finalAmount;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
