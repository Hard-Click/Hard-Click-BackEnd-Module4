package com.wanted.backend.domain.subscription.application.usecase;

public interface CancelSubscriptionUseCase {
    void handle(Long memberId);
}
