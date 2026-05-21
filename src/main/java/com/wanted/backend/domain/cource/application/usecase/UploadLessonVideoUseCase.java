package com.wanted.backend.domain.cource.application.usecase;

import com.wanted.backend.domain.cource.application.command.UploadLessonVideoCommand;

public interface UploadLessonVideoUseCase {
    String handle(UploadLessonVideoCommand command);
}
