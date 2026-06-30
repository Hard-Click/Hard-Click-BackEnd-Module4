package com.wanted.backend.domain.cource.application.usecase;

import com.wanted.backend.domain.cource.application.command.ChangeCourseStatusCommand;
import com.wanted.backend.domain.cource.application.command.ConfirmVideoUploadCommand;
import com.wanted.backend.domain.cource.application.command.CreateCourseCommand;
import com.wanted.backend.domain.cource.application.command.RequestVideoUploadCommand;
import com.wanted.backend.domain.cource.application.command.UpdateCourseCommand;
import com.wanted.backend.domain.cource.application.command.UploadCourseThumbnailCommand;
import com.wanted.backend.domain.cource.application.port.VideoStoragePort;

public interface CourseCommandUseCase {
    Long create(CreateCourseCommand command);
    void update(UpdateCourseCommand command);
    void delete(Long courseId, Long requesterId);
    void changeStatus(ChangeCourseStatusCommand command);
    VideoStoragePort.PresignedUpload requestVideoUpload(RequestVideoUploadCommand command);
    void confirmVideoUpload(ConfirmVideoUploadCommand command);
    String uploadCourseThumbnail(UploadCourseThumbnailCommand command);
}
