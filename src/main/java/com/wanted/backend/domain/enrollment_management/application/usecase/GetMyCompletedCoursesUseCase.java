package com.wanted.backend.domain.enrollment_management.application.usecase;

import java.time.LocalDateTime;
import java.util.List;

public interface GetMyCompletedCoursesUseCase {

    List<MyCompletedCourseView> handle(Long memberId);

    record MyCompletedCourseView(
            Long courseId,
            String courseTitle,
            String thumbnailUrl,
            Integer progressRate,
            LocalDateTime completedAt
    ) {
    }
}
