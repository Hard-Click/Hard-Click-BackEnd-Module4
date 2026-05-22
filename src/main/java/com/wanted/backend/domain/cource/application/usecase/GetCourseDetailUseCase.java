package com.wanted.backend.domain.cource.application.usecase;

import com.wanted.backend.domain.cource.application.dto.CourseDetailResult;

public interface GetCourseDetailUseCase {
    CourseDetailResult handle(Long courseId);
}
