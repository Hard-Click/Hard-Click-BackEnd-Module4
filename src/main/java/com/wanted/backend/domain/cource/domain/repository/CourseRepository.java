package com.wanted.backend.domain.cource.domain.repository;

import com.wanted.backend.domain.cource.domain.model.Course;

import java.util.Optional;

public interface CourseRepository {
    Course save(Course course);
    Optional<Course> findById(Long courseId);
}
