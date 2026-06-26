package com.wanted.backend.domain.notice.infrastructure.course;

import com.wanted.backend.domain.notice.application.port.CourseInfoPort;
import com.wanted.backend.global.exception.BusinessException;
import com.wanted.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
@Transactional(readOnly = true, noRollbackFor = BusinessException.class)
public class CourseInfoAdapter implements CourseInfoPort {

    private final SpringDataCourseReferenceRepository courseReferenceRepository;

    public CourseInfoAdapter(SpringDataCourseReferenceRepository courseReferenceRepository) {
        this.courseReferenceRepository = courseReferenceRepository;
    }

    @Override
    public String getCourseNameByCourseId(Long courseId) {
        return courseReferenceRepository.findById(courseId)
                .map(CourseReferenceEntity::getTitle)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));
    }
    // 코스명 배치 조회 — N+1 방지용
    @Override
    public Map<Long, String> getCourseNamesByCourseIds(List<Long> courseIds) {
        if (courseIds.isEmpty()) return Map.of();
        return courseReferenceRepository.findAllById(courseIds)
                .stream()
                .collect(Collectors.toMap(
                        CourseReferenceEntity::getId,
                        CourseReferenceEntity::getTitle
                ));
    }
}