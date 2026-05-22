package com.wanted.backend.domain.cource.application.command;

import com.wanted.backend.domain.cource.domain.model.CourseStatus;

public record ChangeCourseStatusCommand(
        Long courseId,
        Long requesterId,
        CourseStatus targetStatus
) {}
