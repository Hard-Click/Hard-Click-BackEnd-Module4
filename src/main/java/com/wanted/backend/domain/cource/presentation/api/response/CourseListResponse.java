package com.wanted.backend.domain.cource.presentation.api.response;

import com.wanted.backend.domain.cource.application.dto.CourseListResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "강의 목록 페이징 응답")
public record CourseListResponse(
        @Schema(description = "강의 목록")
        List<CourseListItemResponse> content,

        @Schema(description = "현재 페이지 (0-based)", example = "0")
        int currentPage,

        @Schema(description = "전체 페이지 수", example = "5")
        int totalPages,

        @Schema(description = "전체 강의 수", example = "58")
        long totalCount
) {
    public static CourseListResponse from(CourseListResult result) {
        List<CourseListItemResponse> content = result.items().stream()
                .map(CourseListItemResponse::from)
                .toList();
        return new CourseListResponse(content, result.currentPage(),
                result.totalPages(), result.totalCount());
    }
}
