package com.wanted.backend.domain.enrollment_management.application.usecase;

import java.time.LocalDateTime;
import java.util.List;

public interface GetMyEnrolledCourseUseCase {

    List<MyEnrolledCourseView> handle(Long memberId);

    record MyEnrolledCourseView(
            Long courseId,
            String courseTitle,
            String thumbnailUrl,
            Integer progressRate,
            LocalDateTime lastStudiedAt,
            Long lastVideoId,
            Integer lastPositionSeconds
    ){
    }
}
