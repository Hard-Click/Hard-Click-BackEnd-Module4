package com.wanted.backend.domain.payment.infrastructure.subscription;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Getter
@Immutable
@Table(name = "subscriptions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentSubscriptionReferenceEntity {

    @Id
    @Column(name = "subscription_id")
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "plan_id", nullable = false)
    private Long planId;
}
