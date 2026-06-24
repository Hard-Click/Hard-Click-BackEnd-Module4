package com.wanted.backend.domain.subscription.domain.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Subscription {

    private Long id;
    private final Long memberId;
    private final Long planId;
    private final String paymentMethod;
    private final Integer paidAmount;
    private SubscriptionStatus status;
    private final LocalDateTime startedAt;
    private final LocalDateTime expiredAt;
    private LocalDateTime cancelledAt;
    private final LocalDateTime createdAt;

    private Subscription(Long id, Long memberId, Long planId, String paymentMethod, Integer paidAmount,
                          SubscriptionStatus status, LocalDateTime startedAt, LocalDateTime expiredAt,
                          LocalDateTime cancelledAt, LocalDateTime createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.planId = planId;
        this.paymentMethod = paymentMethod;
        this.paidAmount = paidAmount;
        this.status = status;
        this.startedAt = startedAt;
        this.expiredAt = expiredAt;
        this.cancelledAt = cancelledAt;
        this.createdAt = createdAt;
    }

    public static Subscription create(Long memberId, Long planId, String paymentMethod, Integer paidAmount,
                                        LocalDateTime startedAt, LocalDateTime expiredAt, LocalDateTime now) {
        return new Subscription(null, memberId, planId, paymentMethod, paidAmount,
                SubscriptionStatus.ACTIVE, startedAt, expiredAt, null, now);
    }

    public static Subscription restore(Long id, Long memberId, Long planId, String paymentMethod, Integer paidAmount,
                                        SubscriptionStatus status, LocalDateTime startedAt, LocalDateTime expiredAt,
                                        LocalDateTime cancelledAt, LocalDateTime createdAt) {
        return new Subscription(id, memberId, planId, paymentMethod, paidAmount,
                status, startedAt, expiredAt, cancelledAt, createdAt);
    }

    public void cancel(LocalDateTime cancelledAt) {
        if (this.status != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("ACTIVE 상태에서만 구독 취소가 가능합니다. 현재 상태: " + this.status);
        }
        this.status = SubscriptionStatus.CANCELLED;
        this.cancelledAt = cancelledAt;
    }

    public long remainingDays(LocalDateTime now) {
        if (status != SubscriptionStatus.ACTIVE) {
            return 0;
        }
        // 일 단위 상품이므로 시간 절삭을 피하기 위해 날짜 기준으로 계산
        long days = ChronoUnit.DAYS.between(now.toLocalDate(), expiredAt.toLocalDate());
        return Math.max(days, 0);
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public Long getPlanId() { return planId; }
    public String getPaymentMethod() { return paymentMethod; }
    public Integer getPaidAmount() { return paidAmount; }
    public SubscriptionStatus getStatus() { return status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
