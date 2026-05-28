package com.wanted.backend.domain.notice.application.port;


public interface CourseInstructorPort {
    Long getInstructorIdByCourseId(Long courseId);
}