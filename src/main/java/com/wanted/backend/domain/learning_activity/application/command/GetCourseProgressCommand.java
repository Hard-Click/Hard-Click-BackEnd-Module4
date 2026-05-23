package com.wanted.backend.domain.learning_activity.application.command;

public record GetCourseProgressCommand(
        Long memberId,
        Long courseId
) {
}
