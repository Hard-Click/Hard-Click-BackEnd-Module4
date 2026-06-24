package com.wanted.backend.domain.subscription.application.usecase;

import com.wanted.backend.domain.subscription.application.dto.MySubscriptionResult;

public interface GetMySubscriptionUseCase {
    MySubscriptionResult handle(Long memberId);
}
