package com.wanted.backend.domain.cource.application.port;

public interface InstructorStatsPort {
    int totalStudents(Long authorId);
    int totalCourses(Long authorId);
    double avgRating(Long authorId);
}
