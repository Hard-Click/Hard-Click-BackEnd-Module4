package com.wanted.backend.domain.learning_activity.application.port;

public interface EnrollmentAccessPort {

    boolean hasActiveEnrollment(Long memberId, Long courseId);
}
