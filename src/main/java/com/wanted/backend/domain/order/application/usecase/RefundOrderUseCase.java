package com.wanted.backend.domain.order.application.usecase;

public interface RefundOrderUseCase {

    /**
     * 주문 전체 환불(관리자). 남은 미환불 금액을 PG 취소하고
     * 주문을 REFUNDED로 전이, 수강 권한(또는 구독)을 박탈한다.
     */
    void refundOrder(Long orderId);

    /**
     * 주문 전체 환불(사용자 본인). 소유자 검증 후 refundOrder와 동일하게 처리한다.
     * 구독 주문처럼 항목 단위 환불이 불가능한 주문의 환불 경로로 사용한다.
     */
    void refundOrderByMember(Long memberId, Long orderId);
}
