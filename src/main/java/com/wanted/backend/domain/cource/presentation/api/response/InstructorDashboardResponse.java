package com.wanted.backend.domain.cource.presentation.api.response;

import com.wanted.backend.domain.cource.application.dto.InstructorDashboardResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강사 대시보드 응답")
public record InstructorDashboardResponse(
        @Schema(description = "전체 강의 수", example = "12")
        int totalCourses,

        @Schema(description = "공개 강의 수", example = "9")
        int publishedCourses,

        @Schema(description = "숨김 강의 수", example = "3")
        int hiddenCourses,

        @Schema(description = "수강생 수", example = "245")
        int totalStudents,

        @Schema(description = "퀴즈 수 (현재 quiz 도메인이 Mock API라 임시 고정값)", example = "36")
        int quizCount
) {
    public static InstructorDashboardResponse from(InstructorDashboardResult result) {
        return new InstructorDashboardResponse(
                result.totalCourses(),
                result.publishedCourses(),
                result.hiddenCourses(),
                result.totalStudents(),
                result.quizCount()
        );
    }
}
