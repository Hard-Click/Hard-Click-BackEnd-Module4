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

import java.time.LocalDateTime;

@Entity(name = "WritablePayment")
@Getter
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WritablePaymentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "paid_amount", nullable = false)
    private Integer paidAmount;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "toss_payment_key")
    private String tossPaymentKey;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    public static WritablePaymentJpaEntity create(Long orderId, Long memberId, Integer paidAmount,
                                                   String tossPaymentKey, LocalDateTime paidAt) {
        WritablePaymentJpaEntity e = new WritablePaymentJpaEntity();
        e.orderId = orderId;
        e.memberId = memberId;
        e.paidAmount = paidAmount;
        e.status = "PAID";
        e.tossPaymentKey = tossPaymentKey;
        e.paidAt = paidAt;
        return e;
    }
}
