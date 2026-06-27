package com.wanted.backend.domain.order.infrastructure.subscription;

import com.wanted.backend.domain.order.application.port.OrderSubscriptionCancelPort;
import com.wanted.backend.domain.subscription.application.usecase.CancelSubscriptionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderSubscriptionCancelAdapter implements OrderSubscriptionCancelPort {

    private final CancelSubscriptionUseCase cancelSubscriptionUseCase;

    @Override
    public void cancelByMemberId(Long memberId) {
        cancelSubscriptionUseCase.handle(memberId);
    }
}
