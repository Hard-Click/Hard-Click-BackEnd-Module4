package com.wanted.backend.domain.community.application.port;

public interface EnrollmentCompletedCheckPort {
    boolean isCompleted(Long memberId, Long courseId);
}