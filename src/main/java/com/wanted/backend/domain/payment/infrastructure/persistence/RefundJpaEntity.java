package com.wanted.backend.domain.payment.infrastructure.persistence;

import com.wanted.backend.domain.payment.domain.model.Refund;
import com.wanted.backend.domain.payment.domain.model.RefundStatus;
import com.wanted.backend.domain.payment.domain.model.RefundType;
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

@Entity
@Getter
@Table(name = "refunds")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefundJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "enrollment_id")
    private Long enrollmentId;

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type", nullable = false, length = 20)
    private RefundType refundType;

    @Column(name = "reason")
    private String reason;

    @Column(name = "refund_amount", nullable = false)
    private Integer refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status;

    @Column(name = "refunded_at", nullable = false)
    private LocalDateTime refundedAt;

    static RefundJpaEntity from(Refund refund) {
        RefundJpaEntity entity = new RefundJpaEntity();
        entity.memberId = refund.getMemberId();
        entity.paymentId = refund.getPaymentId();
        entity.enrollmentId = refund.getEnrollmentId();
        entity.subscriptionId = refund.getSubscriptionId();
        entity.refundType = refund.getRefundType();
        entity.reason = refund.getReason();
        entity.refundAmount = refund.getRefundAmount();
        entity.status = refund.getStatus();
        entity.refundedAt = refund.getRefundedAt();
        return entity;
    }

    Refund toDomain() {
        return Refund.restore(id, memberId, paymentId, enrollmentId, subscriptionId,
                refundType, reason, refundAmount, status, refundedAt);
    }
}
