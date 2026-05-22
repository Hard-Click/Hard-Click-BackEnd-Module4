package com.wanted.backend.domain.cource.application.usecase;

import com.wanted.backend.domain.cource.application.command.UpdateCourseCommand;

public interface UpdateCourseUseCase {
    void handle(UpdateCourseCommand command);
}
