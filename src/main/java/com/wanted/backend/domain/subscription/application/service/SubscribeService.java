package com.wanted.backend.domain.subscription.application.service;

import com.wanted.backend.domain.subscription.application.dto.MySubscriptionResult;
import com.wanted.backend.domain.subscription.application.usecase.SubscribeUseCase;
import com.wanted.backend.domain.subscription.domain.model.Subscription;
import com.wanted.backend.domain.subscription.domain.model.SubscriptionPlanCatalog;
import com.wanted.backend.domain.subscription.domain.repository.SubscriptionRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscribeService implements SubscribeUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final Clock clock;

    @Override
    public MySubscriptionResult handle(Long memberId) {
        subscriptionRepository.findActiveByMemberId(memberId).ifPresent(s -> {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_ALREADY_ACTIVE);
        });

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime expiredAt = now.plusDays(SubscriptionPlanCatalog.ANNUAL_PASS_DURATION_DAYS);

        Subscription subscription = Subscription.create(
                memberId,
                SubscriptionPlanCatalog.ANNUAL_PASS_PLAN_ID,
                SubscriptionPlanCatalog.DEFAULT_PAYMENT_METHOD,
                SubscriptionPlanCatalog.ANNUAL_PASS_PRICE,
                now,
                expiredAt,
                now
        );
        Subscription saved = subscriptionRepository.save(subscription);

        return new MySubscriptionResult(
                true,
                saved.getId(),
                saved.getPlanId(),
                saved.getPaymentMethod(),
                saved.getPaidAmount(),
                saved.getStartedAt(),
                saved.getExpiredAt(),
                saved.remainingDays(now)
        );
    }
}
