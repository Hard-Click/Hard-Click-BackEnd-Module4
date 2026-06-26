package com.wanted.backend.domain.cource.application.dto;

public record InstructorDashboardResult(
        int totalCourses,
        int publishedCourses,
        int hiddenCourses,
        int totalStudents,
        int quizCount
) {
}
