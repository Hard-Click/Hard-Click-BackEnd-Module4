package com.wanted.backend.domain.order.application.port;

import java.util.List;
import java.util.Map;

public interface OrderEnrollmentStatusPort {

    /**
     * 회원의 강의별 수강 상태 조회.
     * 값이 없으면(수강 이력 없음) 키 자체가 없을 수 있다.
     */
    Map<Long, String> findEnrollStatuses(Long memberId, List<Long> courseIds);
}
