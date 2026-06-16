package com.wanted.backend.domain.payment.application.port;

import java.util.List;

public interface CourseForOrderQueryPort {
    List<CourseInfo> findAllByIds(List<Long> courseIds);

    record CourseInfo(Long courseId, String title, Integer price) {}
}
