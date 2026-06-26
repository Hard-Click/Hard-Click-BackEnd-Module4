package com.wanted.backend.domain.quiz.application.port;

import java.util.Optional;

public interface CourseTitlePort {

    Optional<String> findTitleByCourseId(Long courseId);
}
