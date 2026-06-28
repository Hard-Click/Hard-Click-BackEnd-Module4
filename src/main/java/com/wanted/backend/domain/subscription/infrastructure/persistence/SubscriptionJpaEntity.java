package com.wanted.backend.domain.subscription.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "subscriptions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_subscription_active_member",
                columnNames = "active_member_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "order_id")
    private Long orderId;

    /**
     * 활성 구독에 한해 memberId를 보관하고, 취소/만료 시 NULL로 비운다.
     * NULL은 유니크 제약에서 제외되므로(MySQL) "회원당 활성 구독 1건"을 DB 레벨에서 강제한다.
     */
    @Column(name = "active_member_id")
    private Long activeMemberId;

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

    public SubscriptionJpaEntity(Long memberId, Long orderId, Long planId, String paymentMethod, Integer paidAmount,
                                  String status, LocalDateTime startedAt, LocalDateTime expiredAt,
                                  LocalDateTime createdAt) {
        this.memberId = memberId;
        this.orderId = orderId;
        this.activeMemberId = "ACTIVE".equals(status) ? memberId : null;
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
        this.activeMemberId = null;
    }
}
