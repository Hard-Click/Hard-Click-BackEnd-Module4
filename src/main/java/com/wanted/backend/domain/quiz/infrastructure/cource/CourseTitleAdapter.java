package com.wanted.backend.domain.quiz.infrastructure.cource;

import com.wanted.backend.domain.cource.domain.model.Course;
import com.wanted.backend.domain.cource.domain.repository.CourseRepository;
import com.wanted.backend.domain.quiz.application.port.CourseTitlePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CourseTitleAdapter implements CourseTitlePort {

    private final CourseRepository courseRepository;

    @Override
    public Optional<String> findTitleByCourseId(Long courseId) {
        return courseRepository.findById(courseId).map(Course::getTitle);
    }
}
