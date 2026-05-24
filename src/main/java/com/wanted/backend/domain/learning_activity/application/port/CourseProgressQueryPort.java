package com.wanted.backend.domain.learning_activity.application.port;

import java.util.List;

public interface CourseProgressQueryPort {

    CourseProgressData findByMemberIdAndCourseId(Long memberId, Long courseId);

    record CourseProgressData(
            Long courseId,
            List<LessonProgressData> lessons
    ) {
    }

    record LessonProgressData(
            Long videoId,
            Boolean completed,
            Integer lastPositionSeconds
    ) {
    }
}
