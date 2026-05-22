package com.wanted.backend.domain.cource.application.service;

import com.wanted.backend.domain.cource.application.usecase.DeleteCourseUseCase;
import com.wanted.backend.domain.cource.domain.model.Course;
import com.wanted.backend.domain.cource.domain.repository.CourseRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCourseService implements DeleteCourseUseCase {

    private final CourseRepository courseRepository;

    @Transactional
    @Override
    public void handle(Long courseId, Long requesterId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (!course.getAuthorId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        courseRepository.delete(courseId);
    }
}
