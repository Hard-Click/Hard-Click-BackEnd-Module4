package com.wanted.backend.domain.subscription.application.usecase;

import com.wanted.backend.domain.subscription.application.dto.MySubscriptionResult;

public interface SubscribeUseCase {

    /**
     * 구독권을 지급한다.
     *
     * @param paidAmount 실제 결제된 금액(주문에 확정된 금액). 동적 가격이므로 재계산하지 않고
     *                   결제 시점에 확정된 금액을 그대로 저장한다.
     */
    MySubscriptionResult handle(Long memberId, int paidAmount);
}
