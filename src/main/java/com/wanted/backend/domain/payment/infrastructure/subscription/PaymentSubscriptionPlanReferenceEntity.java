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
@Table(name = "subscription_plans")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentSubscriptionPlanReferenceEntity {

    @Id
    @Column(name = "plan_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;
}
