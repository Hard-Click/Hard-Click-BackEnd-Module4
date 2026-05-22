package com.wanted.backend.domain.cource.application.usecase;

import com.wanted.backend.domain.cource.application.dto.CourseListResult;
import com.wanted.backend.domain.cource.application.query.CourseListQuery;

public interface GetCourseListUseCase {
    CourseListResult handle(CourseListQuery query);
}
