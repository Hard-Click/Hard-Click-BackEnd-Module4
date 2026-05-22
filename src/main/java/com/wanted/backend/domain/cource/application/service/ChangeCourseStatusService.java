package com.wanted.backend.domain.cource.application.service;

import com.wanted.backend.domain.cource.application.command.ChangeCourseStatusCommand;
import com.wanted.backend.domain.cource.application.usecase.ChangeCourseStatusUseCase;
import com.wanted.backend.domain.cource.domain.model.Course;
import com.wanted.backend.domain.cource.domain.model.CourseStatus;
import com.wanted.backend.domain.cource.domain.repository.CourseRepository;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangeCourseStatusService implements ChangeCourseStatusUseCase {

    private final CourseRepository courseRepository;

    @Transactional
    @Override
    public void handle(ChangeCourseStatusCommand command) {
        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (!course.getAuthorId().equals(command.requesterId())) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        if (command.targetStatus() == CourseStatus.PUBLISHED) {
            course.publish();
        } else {
            course.unpublish();
        }

        courseRepository.save(course);
    }
}
