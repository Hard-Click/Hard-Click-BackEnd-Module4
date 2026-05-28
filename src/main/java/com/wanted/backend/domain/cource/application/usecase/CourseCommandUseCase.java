package com.wanted.backend.domain.cource.application.usecase;

import com.wanted.backend.domain.cource.application.command.ChangeCourseStatusCommand;
import com.wanted.backend.domain.cource.application.command.CreateCourseCommand;
import com.wanted.backend.domain.cource.application.command.UpdateCourseCommand;
import com.wanted.backend.domain.cource.application.command.UploadLessonVideoCommand;

public interface CourseCommandUseCase {
    Long create(CreateCourseCommand command);
    void update(UpdateCourseCommand command);
    void delete(Long courseId, Long requesterId);
    void changeStatus(ChangeCourseStatusCommand command);
    String uploadLessonVideo(UploadLessonVideoCommand command);
}
