package com.wanted.backend.domain.cource.application.usecase;

import com.wanted.backend.domain.cource.application.command.CreateCourseCommand;

public interface CreateCourseUseCase {
    Long handle(CreateCourseCommand command);
}
