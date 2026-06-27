package com.wanted.backend.domain.order.application.port;

import java.util.List;

public interface OrderCartDeletePort {

    void deleteByMemberIdAndCourseIds(Long memberId, List<Long> courseIds);

    void deleteAllByMemberId(Long memberId);
}
