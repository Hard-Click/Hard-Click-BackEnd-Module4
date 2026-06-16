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

@Entity(name = "WritableSubscription")
@Getter
@Table(name = "subscriptions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WritableSubscriptionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    public static WritableSubscriptionJpaEntity create(Long memberId, Long orderId, Long planId,
                                                        LocalDateTime expiredAt) {
        WritableSubscriptionJpaEntity e = new WritableSubscriptionJpaEntity();
        e.memberId = memberId;
        e.orderId = orderId;
        e.planId = planId != null ? planId : 1L;
        e.status = "ACTIVE";
        e.expiredAt = expiredAt;
        return e;
    }
}
