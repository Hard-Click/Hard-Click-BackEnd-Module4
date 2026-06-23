package com.wanted.backend.domain.subscription.application.usecase;

import com.wanted.backend.domain.subscription.application.dto.MySubscriptionResult;

public interface SubscribeUseCase {
    MySubscriptionResult handle(Long memberId);
}
