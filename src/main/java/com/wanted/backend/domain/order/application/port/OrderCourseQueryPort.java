package com.wanted.backend.domain.order.application.port;

import java.util.List;

public interface OrderCourseQueryPort {

    List<CourseInfo> findAllByIds(List<Long> courseIds);

    record CourseInfo(Long courseId, String title, int price, String thumbnailUrl) {}
}
