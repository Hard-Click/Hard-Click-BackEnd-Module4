package com.wanted.backend.domain.payment.presentation.request;

import jakarta.validation.constraints.NotNull;

public record CreateSubscriptionOrderRequest(
        @NotNull(message = "구독 플랜 ID는 필수입니다.")
        Long planId
) {}
