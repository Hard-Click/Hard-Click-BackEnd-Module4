package com.wanted.backend.domain.order.application.usecase;

import com.wanted.backend.domain.order.application.dto.CheckoutResult;
import com.wanted.backend.domain.order.domain.model.OrderType;

import java.util.List;

public interface CheckoutUseCase {

    /**
     * 결제 진입(주문 준비).
     *
     * @param courseId COURSE 단건 결제 시 대상 강의.
     *                 null이면 장바구니 전체 또는 courseIds를 사용한다.
     *
     * @param courseIds 선택 결제 대상 강의 목록.
     *                  null 또는 비어있으면 기존 로직(단건/장바구니 전체)을 사용한다.
     *
     *                  SUBSCRIPTION 타입에서는 모두 무시된다.
     */
    CheckoutResult checkout(
            Long memberId,
            OrderType type,
            Long courseId,
            List<Long> courseIds
    );
}