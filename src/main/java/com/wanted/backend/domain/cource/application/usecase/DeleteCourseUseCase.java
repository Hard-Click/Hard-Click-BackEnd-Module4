package com.wanted.backend.domain.cource.application.usecase;

public interface DeleteCourseUseCase {
    void handle(Long courseId, Long requesterId);
}
