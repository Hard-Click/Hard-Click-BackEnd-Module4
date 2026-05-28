package com.wanted.backend.domain.cource.application.usecase;

import com.wanted.backend.domain.cource.application.dto.CourseDetailResult;
import com.wanted.backend.domain.cource.application.dto.CourseListResult;
import com.wanted.backend.domain.cource.application.query.CourseListQuery;


public interface CourseQueryUseCase {
    CourseListResult getList(CourseListQuery query);
    CourseDetailResult getDetail(Long courseId, Long requesterId);
    CourseListResult getInstructorCourses(Long instructorId, int page, int size);
}
