package com.wanted.backend.domain.cource.application.port;

public interface EnrollmentStatsPort {
    int enrollmentCount(Long courseId);
}
