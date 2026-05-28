package com.wanted.backend.domain.notice.infrastructure.course;

import com.wanted.backend.domain.notice.application.port.CourseInstructorPort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@Transactional(readOnly = true)
public class CourseInstructorAdapter implements CourseInstructorPort {

    private final SpringDataCourseReferenceRepository courseReferenceRepository;

    public CourseInstructorAdapter(SpringDataCourseReferenceRepository courseReferenceRepository) {
        this.courseReferenceRepository = courseReferenceRepository;
    }

    @Override
    public Long getInstructorIdByCourseId(Long courseId) {
        return courseReferenceRepository.findById(courseId)
                .map(CourseReferenceEntity::getAuthorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND2));
    }
}