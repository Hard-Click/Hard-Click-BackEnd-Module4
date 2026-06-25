package com.wanted.backend.domain.order.application.port;

/**
 * 항목 환불 시 수강 권한 박탈 Port.
 * 크로스 컨텍스트 직접 참조 금지 → 이 포트를 통해서만 enrollment 도메인 데이터 변경
 */
public interface OrderEnrollmentRevocationPort {

    void revoke(Long memberId, Long courseId);
}
