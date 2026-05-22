package com.wanted.backend.domain.learning_activity.application.port;

public interface SubscriptionAccessPort {

    boolean hasActiveSubscription(Long memberId);
}
