package com.wanted.backend.domain.order.application.usecase;

public interface RefundOrderUseCase {

    /**
     * 주문 전체 환불(관리자). 남은 미환불 금액을 PG 취소하고
     * 주문을 REFUNDED로 전이, 수강 권한(또는 구독)을 박탈한다.
     */
    void refundOrder(Long orderId);
}
