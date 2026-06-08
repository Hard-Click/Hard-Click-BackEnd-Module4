package com.wanted.backend.domain.notice.infrastructure.course;

import com.wanted.backend.domain.notice.application.port.CourseInfoPort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@Transactional(readOnly = true)
public class CourseInfoAdapter implements CourseInfoPort {

    private final SpringDataCourseInfoReferenceRepository courseInfoReferenceRepository;

    public CourseInfoAdapter(
            SpringDataCourseInfoReferenceRepository courseInfoReferenceRepository) {
        this.courseInfoReferenceRepository = courseInfoReferenceRepository;
    }

    @Override
    public String getCourseNameByCourseId(Long courseId) {
        return courseInfoReferenceRepository.findById(courseId)
                .map(CourseInfoReferenceEntity::getTitle)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));
    }
}