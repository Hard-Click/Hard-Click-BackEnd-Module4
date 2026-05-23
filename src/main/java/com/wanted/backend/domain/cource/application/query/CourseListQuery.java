package com.wanted.backend.domain.cource.application.query;

import com.wanted.backend.domain.cource.domain.model.CourseSortType;

public record CourseListQuery(
        String keyword,         // 강의명 검색 (null = 전체)
        String subject,         // 과목 필터 (null = 전체)
        String instructorName,  // 강사명 필터 (null = 전체)
        CourseSortType sort,    // 정렬 기준
        int page,
        int size
) {}
