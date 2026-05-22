package com.wanted.backend.domain.cource.presentation.api.response;

import com.wanted.backend.domain.cource.application.dto.CourseListResult;

import java.util.List;

public record CourseListResponse(
        List<CourseListItemResponse> courses,
        int currentPage,
        int totalPages,
        long totalCount
) {
    public static CourseListResponse from(CourseListResult result) {
        List<CourseListItemResponse> courses = result.items().stream()
                .map(CourseListItemResponse::from)
                .toList();
        return new CourseListResponse(courses, result.currentPage(),
                result.totalPages(), result.totalCount());
    }
}
