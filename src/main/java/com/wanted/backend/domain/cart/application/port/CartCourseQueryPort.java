package com.wanted.backend.domain.cart.application.port;

import java.util.List;

public interface CartCourseQueryPort {

    List<CourseDetail> findAllByIds(List<Long> courseIds);

    record CourseDetail(
            Long courseId,
            String title,
            Integer price,
            String instructorName
    ) {}
}
