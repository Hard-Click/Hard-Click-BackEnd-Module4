package com.wanted.backend.domain.learning_activity.application.usecase;

import com.wanted.backend.domain.learning_activity.application.command.GetCourseProgressCommand;

import java.math.BigDecimal;
import java.util.List;

public interface GetCourseProgressUseCase {

    CourseProgressView handle(GetCourseProgressCommand command);

    record CourseProgressView(
            Long courseId,
            BigDecimal progressRate,
            Integer completedLessonCount,
            Integer totalLessonCount,
            List<LessonProgressView> lessons
    ) {
    }

    record LessonProgressView(
            Long videoId,
            Boolean completed,
            Integer lastPositionSeconds
    ) {
    }
}
