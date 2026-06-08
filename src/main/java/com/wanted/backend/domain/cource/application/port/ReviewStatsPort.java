package com.wanted.backend.domain.cource.application.port;

public interface ReviewStatsPort {
    double avgRating(Long courseId);
    int reviewCount(Long courseId);
}
