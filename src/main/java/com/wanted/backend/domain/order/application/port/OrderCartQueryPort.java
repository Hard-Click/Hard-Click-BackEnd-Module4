package com.wanted.backend.domain.order.application.port;

import java.util.List;

public interface OrderCartQueryPort {

    /** 회원 장바구니에 담긴 courseId 목록 */
    List<Long> findCartCourseIds(Long memberId);
}
