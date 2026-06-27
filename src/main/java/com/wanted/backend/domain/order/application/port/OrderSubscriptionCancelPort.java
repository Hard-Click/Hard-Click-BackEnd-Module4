package com.wanted.backend.domain.order.application.port;

/**
 * 구독 주문 환불 시 활성 구독을 취소하는 Port.
 * 크로스 컨텍스트 직접 참조 금지 → 이 포트를 통해서만 subscription 도메인 데이터 변경.
 */
public interface OrderSubscriptionCancelPort {

    void cancelByMemberId(Long memberId);
}
