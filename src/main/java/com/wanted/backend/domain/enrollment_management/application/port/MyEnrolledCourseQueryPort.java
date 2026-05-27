package com.wanted.backend.domain.enrollment_management.application.port;

import java.time.LocalDateTime;
import java.util.List;

public interface MyEnrolledCourseQueryPort {

    List<MyEnrolledCourseData> findByMemberId(Long memberId);

    record MyEnrolledCourseData(
            Long courseId,
            String courseTitle,
            String thumbnailUrl,
            Integer completedLessonCount,
            Integer totalLessonCount,
            LocalDateTime lastStudiedAt,
            Long lastVideoId,
            Integer lastPositionSeconds
    ) {
    }
}
