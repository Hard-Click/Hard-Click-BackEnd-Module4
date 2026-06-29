package com.wanted.backend.domain.enrollment_management.presentation.api.response;

import com.wanted.backend.domain.enrollment_management.application.usecase.GetMyEnrolledCourseUseCase;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "마이페이지 수강 중인 강의 항목 응답")
public record MyEnrolledCourseResponse(
        @Schema(description = "강의 ID", example = "5")
        Long courseId,

        @Schema(description = "강의명", example = "2027 수능 수학Ⅱ 미적분 실전 킬러 특강")
        String courseTitle,

        @Schema(description = "강의 썸네일 URL", example = "https://example.com/thumbnail.png")
        String thumbnailUrl,

        @Schema(description = "진도율 (%)", example = "35")
        Integer progressRate,

        @Schema(description = "마지막으로 시청한 영상 ID (이어보기용)", example = "12")
        Long lastVideoId,

        @Schema(description = "마지막 재생 위치 (초)", example = "320")
        Integer lastPositionSeconds,

        @Schema(description = "마지막 학습 일시", example = "2026-06-28T14:30:00")
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
