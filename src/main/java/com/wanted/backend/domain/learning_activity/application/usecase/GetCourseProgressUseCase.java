package com.wanted.backend.domain.learning_activity.application.usecase;

import com.wanted.backend.domain.learning_activity.application.command.GetCourseProgressCommand;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

public interface GetCourseProgressUseCase {

    CourseProgressView handle(GetCourseProgressCommand command);

    @Schema(description = "강의 전체 진도율 응답")
    record CourseProgressView(
            @Schema(description = "강의 ID", example = "20")
            Long courseId,

            @Schema(description = "전체 진도율 (%)", example = "42.86")
            BigDecimal progressRate,

            @Schema(description = "완료한 레슨 수", example = "3")
            Integer completedLessonCount,

            @Schema(description = "전체 레슨 수", example = "7")
            Integer totalLessonCount,

            @Schema(description = "영상별 학습 상태 목록")
            List<LessonProgressView> lessons
    ) {
    }

    @Schema(description = "영상별 학습 상태")
    record LessonProgressView(
            @Schema(description = "영상 ID (lessonId와 동일)", example = "10")
            Long videoId,

            @Schema(description = "완료 여부", example = "true")
            Boolean completed,

            @Schema(description = "마지막 재생 위치 (초)", example = "320")
            Integer lastPositionSeconds
    ) {
    }
}
