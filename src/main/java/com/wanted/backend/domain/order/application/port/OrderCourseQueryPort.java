package com.wanted.backend.domain.order.application.port;

import java.util.List;
import java.util.Map;

public interface OrderCourseQueryPort {

    List<CourseInfo> findAllByIds(List<Long> courseIds);

    /**
     * 주문 상세용 강의 썸네일 조회. courseId → presigned 썸네일 URL.
     * 이미 구매한 강의이므로 PUBLISHED 여부와 무관하게 조회한다.
     */
    Map<Long, String> findThumbnailUrlsByIds(List<Long> courseIds);

    record CourseInfo(Long courseId, String title, int price) {}
}
