package com.wanted.backend.domain.enrollment_management.application.usecase;

import java.time.LocalDateTime;
import java.util.List;

public interface GetMyEnrolledCourseUseCase {

    List<MyEnrolledCourseView> handle(Long memberId);

    record MyEnrolledCourseView(
            Long courseId,
            Long courseTitle,
            String thumbnailUrl,
            Integer progressRate,
            LocalDateTime lastStudiedAt,
            ContinueLessonView continueLesson
    ){
    }

    record ContinueLessonView(
            Long videoId,
            Integer lastPositionSeconds
    ) {
    }
}
