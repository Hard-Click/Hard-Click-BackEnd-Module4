package com.wanted.backend.domain.order.application.usecase;

import com.wanted.backend.domain.order.application.dto.CheckoutResult;
import com.wanted.backend.domain.order.domain.model.OrderType;

public interface CheckoutUseCase {

    /**
     * 결제 진입(주문 준비).
     * @param courseId COURSE 단건 결제 시 대상 강의. null이면 장바구니 전체 결제로 처리.
     *                 SUBSCRIPTION 타입에서는 무시된다.
     */
    CheckoutResult checkout(Long memberId, OrderType type, Long courseId);
}
