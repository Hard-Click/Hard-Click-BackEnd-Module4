package com.wanted.backend.domain.cource.application.port;

public interface InstructorStatsPort {
    int totalStudents(Long authorId);
    int totalCourses(Long authorId);
    double avgRating(Long authorId);

    /** 강사 대시보드용 강의 상태별 집계 (DELETED 제외) */
    CourseCounts courseCounts(Long authorId);

    record CourseCounts(int total, int published, int hidden) {}
}
