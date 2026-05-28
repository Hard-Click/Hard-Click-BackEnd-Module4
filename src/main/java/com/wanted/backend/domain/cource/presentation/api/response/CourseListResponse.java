package com.wanted.backend.domain.cource.presentation.api.response;

import com.wanted.backend.domain.cource.application.dto.CourseListResult;

import java.util.List;

public record CourseListResponse(
        List<CourseListItemResponse> content,
        int currentPage,
        int totalPages,
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
