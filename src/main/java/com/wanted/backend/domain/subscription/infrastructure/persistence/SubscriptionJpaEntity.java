package com.wanted.backend.domain.subscription.infrastructure.persistence;

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
@Table(name = "subscriptions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod;

    @Column(name = "paid_amount", nullable = false)
    private Integer paidAmount;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public SubscriptionJpaEntity(Long memberId, Long planId, String paymentMethod, Integer paidAmount,
                                  String status, LocalDateTime startedAt, LocalDateTime expiredAt,
                                  LocalDateTime createdAt) {
        this.memberId = memberId;
        this.planId = planId;
        this.paymentMethod = paymentMethod;
        this.paidAmount = paidAmount;
        this.status = status;
        this.startedAt = startedAt;
        this.expiredAt = expiredAt;
        this.createdAt = createdAt;
    }

    public void cancel(LocalDateTime cancelledAt) {
        this.status = "CANCELLED";
        this.cancelledAt = cancelledAt;
    }
}
