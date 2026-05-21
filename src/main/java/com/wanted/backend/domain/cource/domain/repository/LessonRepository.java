package com.wanted.backend.domain.cource.domain.repository;

import com.wanted.backend.domain.cource.domain.model.Lesson;

import java.util.Optional;

public interface LessonRepository {
    Lesson save(Lesson lesson);
    Optional<Lesson> findById(Long lessonId);
}
