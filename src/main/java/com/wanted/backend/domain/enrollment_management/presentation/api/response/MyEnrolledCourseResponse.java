package com.wanted.backend.domain.enrollment_management.presentation.api.response;

import com.wanted.backend.domain.enrollment_management.application.usecase.GetMyEnrolledCourseUseCase;

import java.time.LocalDateTime;
import java.util.List;

public record MyEnrolledCourseResponse(
        Long courseId,
        String courseTitle,
        String thumbnailUrl,
        Integer progressRate,
        Long lastVideoId,
        Integer lastPositionSeconds,
        LocalDateTime lastStudiedAt
) {

    public static MyEnrolledCourseResponse from(GetMyEnrolledCourseUseCase.MyEnrolledCourseView view) {
        return new MyEnrolledCourseResponse(
                view.courseId(),
                view.courseTitle(),
                view.thumbnailUrl(),
                view.progressRate(),
                view.lastVideoId(),
                view.lastPositionSeconds(),
                view.lastStudiedAt()
        );
    }

    public static List<MyEnrolledCourseResponse> from(List<GetMyEnrolledCourseUseCase.MyEnrolledCourseView> views) {
        return views.stream()
                .map(MyEnrolledCourseResponse::from)
                .toList();
    }
}
