package com.wanted.backend.domain.enrollment_management.presentation.api.response;

import com.wanted.backend.domain.enrollment_management.application.usecase.GetMyCompletedCoursesUseCase;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record MyCompletedCourseResponse(
        @Schema(description = "강의 ID", example = "20")
        Long courseId,

        @Schema(description = "강의명", example = "React 완벽 가이드")
        String courseTitle,

        @Schema(description = "강의 썸네일 URL", example = "https://example.com/thumb.png")
        String thumbnailUrl,

        @Schema(description = "진도율", example = "100")
        Integer progressRate,

        @Schema(description = "완료 일시", example = "2026-05-28T10:30:00")
        LocalDateTime completedAt
) {

    public static MyCompletedCourseResponse from(GetMyCompletedCoursesUseCase.MyCompletedCourseView view) {
        return new MyCompletedCourseResponse(
                view.courseId(),
                view.courseTitle(),
                view.thumbnailUrl(),
                view.progressRate(),
                view.completedAt()
        );
    }

    public static List<MyCompletedCourseResponse> from(List<GetMyCompletedCoursesUseCase.MyCompletedCourseView> views) {
        return views.stream()
                .map(MyCompletedCourseResponse::from)
                .toList();
    }
}
