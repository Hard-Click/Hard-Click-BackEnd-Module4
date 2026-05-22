package com.wanted.backend.domain.cource.application.usecase;

import com.wanted.backend.domain.cource.application.command.ChangeCourseStatusCommand;

public interface ChangeCourseStatusUseCase {
    void handle(ChangeCourseStatusCommand command);
}
