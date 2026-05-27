package com.wanted.backend.domain.enrollment_management.presentation.api.response;

import com.wanted.backend.domain.enrollment_management.application.usecase.GetMyEnrolledCourseUseCase;

import java.util.List;

public record MyEnrolledCourseResponse(
        Long courseId,
        String courseTitle,
        String thumbnailUrl,
        String instructorName,
        Long progressRate,
        Long lastVideoId,
        Long lastPositionSeconds,
        String lastStudiedAt
) {
    public static List<MyEnrolledCourseResponse> change(List<GetMyEnrolledCourseUseCase.MyEnrolledCourseView> handle) {

       return null;
    }
}

