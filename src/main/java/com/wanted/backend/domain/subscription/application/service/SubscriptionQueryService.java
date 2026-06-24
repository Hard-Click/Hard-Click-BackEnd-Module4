package com.wanted.backend.domain.subscription.application.service;

import com.wanted.backend.domain.subscription.application.dto.MySubscriptionResult;
import com.wanted.backend.domain.subscription.application.dto.SubscriptionPlanResult;
import com.wanted.backend.domain.subscription.application.usecase.GetMySubscriptionUseCase;
import com.wanted.backend.domain.subscription.application.usecase.GetSubscriptionPlanUseCase;
import com.wanted.backend.domain.subscription.domain.model.Subscription;
import com.wanted.backend.domain.subscription.domain.model.SubscriptionPlanCatalog;
import com.wanted.backend.domain.subscription.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionQueryService implements GetSubscriptionPlanUseCase, GetMySubscriptionUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final Clock clock;

    @Override
    public SubscriptionPlanResult handle() {
        return new SubscriptionPlanResult(
                SubscriptionPlanCatalog.ANNUAL_PASS_PLAN_ID,
                SubscriptionPlanCatalog.ANNUAL_PASS_NAME,
                SubscriptionPlanCatalog.ANNUAL_PASS_PRICE,
                SubscriptionPlanCatalog.ANNUAL_PASS_DURATION_DAYS,
                SubscriptionPlanCatalog.ANNUAL_PASS_BENEFITS
        );
    }

    @Override
    public MySubscriptionResult handle(Long memberId) {
        return subscriptionRepository.findActiveByMemberId(memberId)
                .map(this::toResult)
                .orElseGet(MySubscriptionResult::notSubscribed);
    }

    private MySubscriptionResult toResult(Subscription subscription) {
        LocalDateTime now = LocalDateTime.now(clock);
        return new MySubscriptionResult(
                true,
                subscription.getId(),
                subscription.getPlanId(),
                subscription.getPaymentMethod(),
                subscription.getPaidAmount(),
                subscription.getStartedAt(),
                subscription.getExpiredAt(),
                subscription.remainingDays(now)
        );
    }
}
